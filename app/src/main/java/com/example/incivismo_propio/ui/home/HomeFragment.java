package com.example.incivismo_propio.ui.home;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.incivismo_propio.R;
import com.example.incivismo_propio.Reporte;
import com.example.incivismo_propio.databinding.FragmentHomeBinding;
import com.example.incivismo_propio.ui.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    //creo una instancia


    private ActivityResultLauncher<String[]> locationPermissionRequest;

    private FragmentHomeBinding binding;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mTrackingLocation;
    private LocationCallback mLocationCallback;
    private FirebaseUser authUser;
    String mCurrentPhotoPath;
    private Uri photoURI;
    private ImageView foto;
    static final int REQUEST_TAKE_PHOTO = 1;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedViewModel homeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        SharedViewModel.getCurrentAddress().observe(getViewLifecycleOwner(), address -> {
            binding.editTextUbi.setText(String.format(
                    "Direcció: %1$s \n Hora: %2$tr",
                    address, System.currentTimeMillis())
            );
        });
        //lo se usa aqui también se puede usar en el notification
        sharedViewModel.getCurrentLatLng().observe(getViewLifecycleOwner(), latlng -> {
            binding.editTextLongitud.setText(String.valueOf(latlng.latitude));
            binding.editTextLatitud.setText(String.valueOf(latlng.longitude));
        });

/*
        sharedViewModel.getProgressBar().observe(getViewLifecycleOwner(), visible -> {
            if (visible)
                binding.loading.setVisibility(ProgressBar.VISIBLE);
            else
                binding.loading.setVisibility(ProgressBar.INVISIBLE);
        });
*/

        sharedViewModel.switchTrackingLocation();

        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;
        });

        binding.buttonReportar.setOnClickListener(button -> {
            Reporte reporte = new Reporte();
            reporte.setUbicacion(binding.editTextUbi.getText().toString());
            //para que si hay un espacio lo junte todo el .trim
            double latitud = Double.parseDouble(binding.editTextLatitud.getText().toString().trim());
            double longitud = Double.parseDouble(binding.editTextLongitud.getText().toString().trim());
            reporte.setProblema(binding.editTextProblema.getText().toString());

            reporte.setLongitud(longitud);
            reporte.setLatitud(latitud);

            DatabaseReference base = FirebaseDatabase.getInstance().getReference();
            DatabaseReference users = base.child("users");
            DatabaseReference uid = users.child(authUser.getUid());
            DatabaseReference incidencies = uid.child("incidencies");

            DatabaseReference reference = incidencies.push();
            reference.setValue(reporte);
        });

        ImageView Foto = root.findViewById(R.id.foto);
        Button buttonFoto = root.findViewById(R.id.button_foto);

        buttonFoto.setOnClickListener(button -> {
            dispatchTakePictureIntent();
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
        //lo comento para que no salga la ubi
        //binding.editTextUbi.setText("Carregant...");
        //binding.loading.setVisibility(ProgressBar.VISIBLE);
        mTrackingLocation = true;
        //binding.buttonBuscarCampos.setText("Aturar el seguiment de la ubicació");
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
            //binding.buttonBuscarCampos.setText("Comença a seguir la ubicació");
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir


        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(
                getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(photoURI).into(foto);
            } else {
                Toast.makeText(getContext(),
                        "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }


        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}