package com.example.incivismo_propio.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.incivismo_propio.Reporte;
import com.example.incivismo_propio.databinding.FragmentDashboardBinding;

import com.example.incivismo_propio.databinding.RvReportesBinding;
import com.example.incivismo_propio.ui.SharedViewModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    //ususario de firebase
    private FirebaseUser authUser;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        SharedViewModel sharedViewModel = new ViewModelProvider(
                requireActivity()
        ).get(SharedViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;

            //aqui obteenmos el usuario y cogemos los datos desde el firebase

            if (user != null) {
                DatabaseReference base = FirebaseDatabase.getInstance().getReference();
                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(user.getUid());
                DatabaseReference reporte = uid.child("reportes");

                FirebaseRecyclerOptions<Reporte> options = new FirebaseRecyclerOptions.Builder<Reporte>()
                        .setQuery(reporte, Reporte.class)
                        .setLifecycleOwner(this)
                        .build();

                ReporteAdapter adapter = new ReporteAdapter(options);
                binding.rvReportes.setAdapter(adapter);
                binding.rvReportes.setLayoutManager(new LinearLayoutManager(requireContext()));
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class ReporteAdapter extends FirebaseRecyclerAdapter<Reporte, ReporteAdapter.ReporteViewholder> {
        public ReporteAdapter(@NonNull FirebaseRecyclerOptions<Reporte> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(
        @NonNull ReporteViewholder holder, int position, @NonNull Reporte model
            ) {

            holder.binding.txtDescripcio.setText(model.getProblema());
            holder.binding.txtAdreca.setText(model.getUbicacion());
        }

        @NonNull
        @Override
        public ReporteViewholder onCreateViewHolder(
        @NonNull ViewGroup parent, int viewType
            ) {
            return new ReporteViewholder(RvReportesBinding
                    .inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false));
        }

        class ReporteViewholder extends RecyclerView.ViewHolder {
            RvReportesBinding
                    binding;

            public ReporteViewholder(RvReportesBinding
                                                binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
