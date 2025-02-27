package com.example.incivismo_propio.ui.notifications;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.incivismo_propio.R;
import com.example.incivismo_propio.Reporte;
import com.example.incivismo_propio.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference camposFutbolRef;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configuración de OpenStreetMap
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Configurar el mapa
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        IMapController mapController = binding.map.getController();
        mapController.setZoom(7.5);

        //españa
        GeoPoint españa = new GeoPoint(40.4531, -3.6883);
        mapController.setCenter(españa);

        // Añadir marcador en el Bernabéu
        /*Marker startMarker = new Marker(binding.map);
        startMarker.setPosition(bernabeu);
        startMarker.setTitle("Santiago Bernabéu");
        binding.map.getOverlays().add(startMarker);*/

        // Mostrar ubicación del usuario
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), binding.map);
        myLocationOverlay.enableMyLocation();
        binding.map.getOverlays().add(myLocationOverlay);

        // Agregar brújula
        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), binding.map);
        compassOverlay.enableCompass();
        binding.map.getOverlays().add(compassOverlay);

        auth = FirebaseAuth.getInstance();

        // Conectar a Firebase y obtener la referencia de los campos de fútbol
        DatabaseReference base = FirebaseDatabase.getInstance().getReference();
        //quito los campos y lo cojo de lo que reporto
        DatabaseReference users = base.child("users");

        DatabaseReference baseUID = users.child(auth.getUid());

        camposFutbolRef = baseUID.child("incidencies");

        Log.d("III",  camposFutbolRef.toString());


        // Cargar marcadores de campos de fútbol desde Firebase
        camposFutbolRef.addChildEventListener(new ChildEventListener() {

            //yo aqui le he dicho que lo haga solo una vez, tengo en firebase que cerrarlo y que lo vuelva a abrir y salga
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (snapshot.exists()) {
                    //si no es un numero se cierra
                    Reporte reporte = snapshot.getValue(Reporte.class);
                    String nombre = snapshot.child("problema").getValue(String.class);
                    Double latitud = snapshot.child("latitud").getValue(Double.class);
                    Double longitud = snapshot.child("longitud").getValue(Double.class);
                    Log.d("BB", latitud + " " + longitud);

                        if (reporte != null) {
                            GeoPoint location = new GeoPoint(latitud, longitud);

                            Marker marker = new Marker(binding.map);
                            marker.setPosition(location);
                            marker.setTitle(nombre);
                            marker.setIcon(requireContext().getDrawable(R.drawable.ic_marca_campo)); //es el bicho eso
                            marker.setSnippet(reporte.getUbicacion());

                            binding.map.getOverlays().add(marker);
                            //binding.map.invalidate();
                        }
                    }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
