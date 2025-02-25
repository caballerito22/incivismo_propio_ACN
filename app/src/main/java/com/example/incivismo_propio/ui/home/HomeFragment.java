package com.example.incivismo_propio.ui.home;

import static android.content.ContentValues.TAG;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.incivismo_propio.Reporte;
import com.example.incivismo_propio.databinding.FragmentHomeBinding;
import com.example.incivismo_propio.ui.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    //creo una instancia
    private FirebaseAuth auth;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mTrackingLocation;

    private LocationCallback mLocationCallback;

    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        //PARA CONVERTIR LOS ELEMENTOS DEL fragment_home EN VISTA (LOS INFLA)
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //obtiene los views model para los datos
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        //obtiene la direccion y la actualoza
        SharedViewModel.getCurrentAddress().observe(getViewLifecycleOwner(), address -> {
            binding.editTextUbi.setText(String.format(
                    "Direcció: %1$s \n Hora: %2$tr",
                    address, System.currentTimeMillis()));
        });

        //para poner la latitud y la longitud
        sharedViewModel.getCurrentLatLng().observe(getViewLifecycleOwner(), latlng -> {
            binding.editTextLatitud.setText(String.valueOf(latlng.latitude));
            binding.editTextLongitud.setText(String.valueOf(latlng.longitude));
        });

        //obtiene un string del sharedViewModel, obrerva cambios, y lo actualiza (cambia el texto del botón solo)
        sharedViewModel.getButtonText().observe(getViewLifecycleOwner(), s -> binding.buttonBuscarCampos.setText(s));
        //si está visible muestra la barra, si no no
       /* sharedViewModel.getProgressBar().observe(getViewLifecycleOwner(), visible -> {
            if (visible)
                binding.loading.setVisibility(ProgressBar.VISIBLE);
            else
                binding.loading.setVisibility(ProgressBar.INVISIBLE);
        });*/

        //cuando le damos al boton sale el mensaje
        binding.buttonBuscarCampos.setOnClickListener(view -> {
            Log.d("DEBUG", "Obtenemos ubi");
            sharedViewModel.switchTrackingLocation();
        });

        //Agregar evento al botón de reportar
        binding.buttonReportar.setOnClickListener(button -> {
            Reporte reporte = new Reporte();
            reporte.setUbicacion(binding.editTextUbi.getText().toString());
            reporte.setLatitud(binding.editTextLatitud.getText().toString());
            reporte.setLongitud(binding.editTextLongitud.getText().toString());
            reporte.setProblema(binding.editTextProblema.getText().toString());

            //conectamos a firebase
            auth = FirebaseAuth.getInstance();
            DatabaseReference base = FirebaseDatabase.getInstance().getReference();
            DatabaseReference users = base.child("users");
            DatabaseReference uid = users.child(auth.getUid());
            DatabaseReference incidencies = uid.child("incidencies");

            DatabaseReference reference = incidencies.push();
            reference.setValue(reporte);
        });


        return root;
    }

    /*
        //iniciamos mFusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());


        //mira a ver si tiene permisos
        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    //si estan disponible obtengo la localización
                    if (fineLocationGranted != null && fineLocationGranted) {
                        startTrackingLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        startTrackingLocation();
                        //si no se lo digo
                    } else {
                        Toast.makeText(requireContext(), "No hay permiso", Toast.LENGTH_SHORT).show();
                    }
                });

        //para recibir las actualizaciones de ubicaCIÓN
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    fetchAddress(locationResult.getLastLocation());
                } else {
                    binding.localitzacio.setText("Sense localització coneguda");
                }
            }
        };

        binding.buttonLocation.setOnClickListener(view -> {
            if (!mTrackingLocation) {
                Toast.makeText(requireContext(), "Obtenemos la ubi", Toast.LENGTH_SHORT).show();
                startTrackingLocation();
            } else {
                Toast.makeText(requireContext(), "Paramos la ubi", Toast.LENGTH_SHORT).show();
                stopTrackingLocation();
            }
        });




        return root;
    }
    */


    //getLocation
    private void startTrackingLocation() {
        //comprobamos permisosos
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "pidiendo permisos", Toast.LENGTH_SHORT).show();
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            Toast.makeText(requireContext(), "permisos aceptados", Toast.LENGTH_SHORT).show();
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
        }
        binding.editTextUbi.setText("Carregant...");
        //binding.loading.setVisibility(ProgressBar.VISIBLE);
        mTrackingLocation = true;
        binding.buttonBuscarCampos.setText("Aturar el seguiment de la ubicació");
    }

    //coverte la localizacion en texto, si no, nos dice el erroe
    private void fetchAddress(Location location) {

        ExecutorService executor = Executors.newSingleThreadExecutor();


        //creamos un handler para comunicar
        Handler handler = new Handler(Looper.getMainLooper());

        Geocoder geocoder = new Geocoder(requireContext(),
                Locale.getDefault());

        executor.execute(() -> {
        //dirección y resultado final/mensaje de error
        List<Address> addresses = null;
        String resultMessage = "";

        //intentamos abrir una lista de direcciones de localizacion
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    //número de direcciones a leer
                    1);
            //comprovamos la lista de direcciones
            if (addresses == null || addresses.size() == 0) {
                if (resultMessage.isEmpty()) {
                    resultMessage = "No s'ha trobat cap adreça";
                    Log.e("INCIVISME", resultMessage);
                }
            }else {
                //esto es para poner la dirección en una cadena línea por línea
                Address address = addresses.get(0);
                ArrayList<String> addressParts = new ArrayList<>();

                //itera y los guarda en una cadena línea por línea
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressParts.add(address.getAddressLine(i));
                }

                //convierte la lista en una cadena y los junta con un salto de línea
                resultMessage = TextUtils.join("\n", addressParts);

                //actualizamos el texView con la direccion y la hora
                String finalResultMessage = resultMessage;
                handler.post(() -> {
                    //por si desactiva las actualizaciones de localizacion
                    if (mTrackingLocation) {
                        // Aquest codi s'executa en primer pla.
                        binding.editTextUbi.setText(String.format("Direcció: %1$s \n Hora: %2$tr", finalResultMessage, System.currentTimeMillis()));
                    }
                });
            }

            //si hay un error de red o con geocoder
        } catch (IOException ioException) {
            resultMessage = "Servei no disponible";
            Log.e(TAG, resultMessage, ioException);
            //si las cordenadas no están bien y log con el resultado
        }catch (IllegalArgumentException illegalArgumentException) {
            resultMessage = "Coordenadas invalidas";
            Log.e(TAG, resultMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }
        });
    }


    //requisitos para las solicitudes de ubicación
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            //binding.loading.setVisibility(ProgressBar.INVISIBLE);
            mTrackingLocation = false;
            binding.buttonBuscarCampos.setText("Comença a seguir la ubicació");
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        }
    }


        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}