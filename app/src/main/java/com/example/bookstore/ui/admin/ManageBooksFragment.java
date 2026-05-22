package com.example.bookstore.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.bookstore.data.model.Book;
import com.example.appbansach.databinding.FragmentManageBooksBinding;
import com.example.appbansach.utils.Resource;

import java.util.ArrayList;

public class ManageBooksFragment extends Fragment {
    private FragmentManageBooksBinding binding;
    private AdminViewModel viewModel;
    private AdminBookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        setupRecyclerView();
        observeViewModel();

        binding.fabAddBook.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_manageBooksFragment_to_addEditBookFragment)
        );

        viewModel.loadAllBooks();
    }

    private void setupRecyclerView() {
        adapter = new AdminBookAdapter(new ArrayList<>(), new AdminBookAdapter.OnAdminBookClickListener() {
            @Override
            public void onEditClick(Book book) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("book", book);
                Navigation.findNavController(requireView()).navigate(R.id.action_manageBooksFragment_to_addEditBookFragment, bundle);
            }

            @Override
            public void onDeleteClick(Book book) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa sách")
                        .setMessage("Bạn có chắc chắn muốn xóa cuốn sách này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteBook(book.getId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        binding.rvAdminBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminBooks.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.allBooks.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        setupRecyclerViewWithData(resource.data);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.operationStatus.observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), resource.data, Toast.LENGTH_SHORT).show();
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViewWithData(java.util.List<Book> books) {
        adapter = new AdminBookAdapter(books, new AdminBookAdapter.OnAdminBookClickListener() {
            @Override
            public void onEditClick(Book book) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("book", book);
                Navigation.findNavController(requireView()).navigate(R.id.action_manageBooksFragment_to_addEditBookFragment, bundle);
            }

            @Override
            public void onDeleteClick(Book book) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa sách")
                        .setMessage("Bạn có chắc chắn muốn xóa cuốn sách này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteBook(book.getId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        binding.rvAdminBooks.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
