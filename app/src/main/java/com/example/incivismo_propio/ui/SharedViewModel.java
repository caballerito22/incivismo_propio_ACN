package com.example.incivismo_propio.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedViewModel extends AndroidViewModel {
    //para obtener la latutud y la longitud
    private final MutableLiveData<LatLng> currentLatLng = new MutableLiveData<>();

    private final Application app;
    //direccion actual
    private static final MutableLiveData<String> currentAddress = new MutableLiveData<>();
    //verificar si hay permisos
    private final MutableLiveData<String> checkPermission = new MutableLiveData<>();
    //texto de empieza o para la ubi
    private final MutableLiveData<String> buttonText = new MutableLiveData<>();
    //mostrar la barra dependioendo de si está activada la ubi
    private final MutableLiveData<Boolean> progressBar = new MutableLiveData<>();

    private boolean mTrackingLocation;
    FusedLocationProviderClient mFusedLocationClient;

    //codigo para guardar usuario logeado de firebase
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();

    //retornamos el livedata de la latitud y la longitud
    public MutableLiveData<LatLng> getCurrentLatLng() {
        return currentLatLng;
    }
    public LiveData<FirebaseUser> getUser() {
        return user;
    }
    public void setUser(FirebaseUser passedUser) {
        user.postValue(passedUser);
    }

    public SharedViewModel(@NonNull Application application) {
        super(application);

        this.app = application;

    }

    //pongo public para que en el main no de error
    public void setFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    //los getters en el fragmenti i en la actividad (activity)
    public static LiveData<String> getCurrentAddress() {
        return currentAddress;
    }


    //pongo public para que en el main no de error
    //live para comprobar los permisos
    public LiveData<String> getCheckPermission() {
        return checkPermission;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                fetchAddress(locationResult.getLastLocation());
            }
        }
    };

    //requisitos para las solicitudes de ubicación, esto devuelve la solicitud para la localizacion
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    //para que intercambie la localización, en el homeFragment se usa
    public void switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(true);
        } else {
            stopTrackingLocation();
        }

    }

    //pongo public para que en el main no de error
    //inicia el seguimiento
    @SuppressLint("MissingPermission")
    public void startTrackingLocation(boolean needsChecking) {
        //si no tiene permisos lo comprovamos
        if (needsChecking) {
            checkPermission.postValue("check");
        } else {
            //si ya los tiene...
            mFusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    mLocationCallback, null
            );

            //he quitado que cuando lo ejecute me salga la ubi
            //currentAddress.postValue("Carregant...");

            progressBar.postValue(true);
            mTrackingLocation = true;
            buttonText.setValue("Aturar el seguiment de la ubicació");
        }
    }


    //para el seguimiento
    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mTrackingLocation = false;
            progressBar.postValue(false);
            buttonText.setValue("Comença a seguir la ubicació");
        }
    }

    //obtiene la localizacion
    private void fetchAddress(Location location) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Geocoder geocoder = new Geocoder(app.getApplicationContext(), Locale.getDefault());

        executor.execute(() -> {
            // Aquest codi s'executa en segon pla
            List<Address> addresses = null;
            String resultMessage = "";

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),
                        // En aquest cas, sols volem una única adreça:
                        1);


                if (addresses == null || addresses.size() == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "No s'ha trobat cap adreça";
                        Log.e("INCIVISME", resultMessage);
                    }
                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressParts.add(address.getAddressLine(i));
                    }

                    resultMessage = TextUtils.join("\n", addressParts);
                    String finalResultMessage = resultMessage;
                   /* handler.post(() -> {
                        // Aquest codi s'executa en primer pla.
                        if (mTrackingLocation) {
                            currentAddress.postValue(String.format("Direcció: %1$s \n Hora: %2$tr", finalResultMessage, System.currentTimeMillis()));
                        }
                        //actualizamos latitud y longitud
                        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                        currentLatLng.postValue(latlng);
                    });*/
                }

            } catch (IOException ioException) {
                resultMessage = "Servei no disponible";
                Log.e("INCIVISME", resultMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                resultMessage = "Coordenades no vàlides";
                Log.e("INCIVISME", resultMessage + ". " + "Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude(), illegalArgumentException);
            }
        });
    }
}