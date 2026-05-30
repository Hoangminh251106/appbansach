package com.example.appbansach.fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.SoldBookAdapter;
import com.example.appbansach.databinding.FragmentStatisticsBinding;
import com.example.appbansach.model.Order;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private FirebaseFirestore db;
    private SoldBookAdapter soldBookAdapter;
    private List<Map.Entry<String, Integer>> soldBooksList = new ArrayList<>();
    private Calendar selectedCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupChart();
        setupRecyclerView();
        
        // Mặc định load ngày hôm nay
        loadStatisticsForDate(selectedCalendar.getTime());

        binding.btnSelectDate.setOnClickListener(v -> showDatePicker());

        return binding.getRoot();
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            Date date = selectedCalendar.getTime();
            binding.tvSelectedDate.setText("Ngày: " + dateFormat.format(date));
            loadStatisticsForDate(date);
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupChart() {
        binding.barChart.setDrawBarShadow(false);
        binding.barChart.setDrawValueAboveBar(true);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.setPinchZoom(false);
        binding.barChart.setDrawGridBackground(false);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0); // Vì là HorizontalBarChart nên nhãn nằm dọc bên trái

        binding.barChart.getAxisLeft().setDrawGridLines(true);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
    }

    private void setupRecyclerView() {
        soldBookAdapter = new SoldBookAdapter(soldBooksList);
        binding.rvSoldBooksDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSoldBooksDetail.setAdapter(soldBookAdapter);
    }

    private void loadStatisticsForDate(Date date) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date end = cal.getTime();

        db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(start))
                .whereLessThan("createdAt", new Timestamp(end))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded() || binding == null) return;
            binding.progressBar.setVisibility(View.GONE);

            long totalRevenue = 0;
            int totalOrders = queryDocumentSnapshots.size();
            int totalBooksSold = 0;
            Map<String, Integer> bookSalesMap = new HashMap<>();

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                
                // Chỉ tính doanh thu đơn đã giao
                if ("delivered".equals(order.getStatus())) {
                    totalRevenue += order.getTotalAmount();
                }

                // Thống kê sách bán cho tất cả đơn trừ đơn đã hủy
                if (!"cancelled".equals(order.getStatus())) {
                    if (order.getItems() != null) {
                        for (Map<String, Object> item : order.getItems()) {
                            String title = (String) item.get("title");
                            Object qtyObj = item.get("quantity");
                            int quantity = 0;
                            if (qtyObj instanceof Long) quantity = ((Long) qtyObj).intValue();
                            else if (qtyObj instanceof Integer) quantity = (Integer) qtyObj;

                            totalBooksSold += quantity;
                            
                            if (title != null) {
                                bookSalesMap.put(title, bookSalesMap.getOrDefault(title, 0) + quantity);
                            }
                        }
                    }
                }
            }

            DecimalFormat formatter = new DecimalFormat("#,###");
            binding.tvTotalRevenue.setText(formatter.format(totalRevenue) + "đ");
            binding.tvTotalOrdersCount.setText(String.valueOf(totalOrders));
            binding.tvTotalBooksSold.setText(String.valueOf(totalBooksSold));

            updateChartAndList(bookSalesMap);
        }).addOnFailureListener(e -> {
            if (binding != null) binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void updateChartAndList(Map<String, Integer> bookSalesMap) {
        if (bookSalesMap.isEmpty()) {
            binding.barChart.clear();
            binding.barChart.setNoDataText("Không có dữ liệu cho ngày này");
            soldBooksList.clear();
            soldBookAdapter.notifyDataSetChanged();
            return;
        }

        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(bookSalesMap.entrySet());
        Collections.sort(sortedList, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Cập nhật danh sách chi tiết
        soldBooksList.clear();
        soldBooksList.addAll(sortedList);
        soldBookAdapter.notifyDataSetChanged();

        // Cập nhật Biểu đồ (Top 5 hoặc Top 10)
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < Math.min(sortedList.size(), 10); i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            // i là index, entry.getValue() là trục giá trị
            entries.add(new BarEntry(i, entry.getValue()));
            
            String title = entry.getKey();
            String shortTitle = title.length() > 15 ? title.substring(0, 13) + ".." : title;
            labels.add(shortTitle);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Số lượng đã bán");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChart.setData(data);
        binding.barChart.invalidate();
        binding.barChart.animateY(1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
