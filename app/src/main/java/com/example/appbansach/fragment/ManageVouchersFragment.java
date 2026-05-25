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
import com.example.appbansach.databinding.FragmentManageVouchersBinding;
import com.example.appbansach.model.Voucher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageVouchersFragment extends Fragment {
    private FragmentManageVouchersBinding binding;
    private AdminVoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageVouchersBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadVouchers();

        binding.fabAddVoucher.setOnClickListener(v -> showAddVoucherDialog());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminVoucherAdapter(voucherList, voucher -> {
            db.collection("vouchers").document(voucher.getCode()).delete()
                    .addOnSuccessListener(v -> Toast.makeText(getContext(), "Đã xóa mã", Toast.LENGTH_SHORT).show());
        });
        binding.rvAdminVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminVouchers.setAdapter(adapter);
    }

    private void loadVouchers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("vouchers").addSnapshotListener((value, error) -> {
            if (!isAdded()) return;
            binding.progressBar.setVisibility(View.GONE);
            if (value != null) {
                voucherList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Voucher v = doc.toObject(Voucher.class);
                    voucherList.add(v);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddVoucherDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etCode = new EditText(requireContext());
        etCode.setHint("Mã giảm giá (VD: GIAM50K)");
        layout.addView(etCode);

        final EditText etAmount = new EditText(requireContext());
        etAmount.setHint("Số tiền giảm (đ)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etAmount);

        final EditText etMin = new EditText(requireContext());
        etMin.setHint("Đơn hàng tối thiểu (đ)");
        etMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etMin);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm Mã giảm giá mới")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String code = etCode.getText().toString().trim().toUpperCase();
                    String amount = etAmount.getText().toString().trim();
                    String min = etMin.getText().toString().trim();

                    if (!code.isEmpty() && !amount.isEmpty() && !min.isEmpty()) {
                        Voucher v = new Voucher(code, Long.parseLong(amount), Long.parseLong(min));
                        db.collection("vouchers").document(code).set(v);
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
