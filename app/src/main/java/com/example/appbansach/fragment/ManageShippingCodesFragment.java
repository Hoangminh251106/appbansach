package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.AdminVoucherAdapter;
import com.example.appbansach.databinding.FragmentManageShippingCodesBinding;
import com.example.appbansach.model.Voucher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageShippingCodesFragment extends Fragment {
    private FragmentManageShippingCodesBinding binding;
    private AdminVoucherAdapter adapter;
    private List<Voucher> codeList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageShippingCodesBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        binding.toolbar.setTitle("Quản lý mã vận chuyển");
        setupRecyclerView();
        loadShippingCodes();

        binding.fabAddShippingCode.setOnClickListener(v -> showAddCodeDialog());
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminVoucherAdapter(codeList, voucher -> {
            db.collection("shipping_codes").document(voucher.getCode()).delete()
                    .addOnSuccessListener(v -> Toast.makeText(getContext(), "Đã xóa mã vận chuyển", Toast.LENGTH_SHORT).show());
        });
        binding.rvShippingCodes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvShippingCodes.setAdapter(adapter);
    }

    private void loadShippingCodes() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("shipping_codes").addSnapshotListener((value, error) -> {
            if (!isAdded()) return;
            binding.progressBar.setVisibility(View.GONE);
            if (value != null) {
                codeList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Voucher v = doc.toObject(Voucher.class);
                    codeList.add(v);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddCodeDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etCode = new EditText(requireContext());
        etCode.setHint("Mã vận chuyển (VD: FREESHIP)");
        layout.addView(etCode);

        final EditText etAmount = new EditText(requireContext());
        etAmount.setHint("Số tiền giảm phí ship (đ)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etAmount);

        final EditText etMin = new EditText(requireContext());
        etMin.setHint("Đơn hàng tối thiểu (đ)");
        etMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etMin);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm Mã vận chuyển mới")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String code = etCode.getText().toString().trim().toUpperCase();
                    String amountStr = etAmount.getText().toString().trim();
                    String minStr = etMin.getText().toString().trim();

                    if (!code.isEmpty() && !amountStr.isEmpty() && !minStr.isEmpty()) {
                        Voucher v = new Voucher(code, Long.parseLong(amountStr), Long.parseLong(minStr));
                        db.collection("shipping_codes").document(code).set(v);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
