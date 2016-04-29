package com.example.sandra.testcontactagym;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;


import com.firebase.ui.FirebaseListAdapter;



public class MainActivity extends AppCompatActivity {
    private Firebase infoGymRef;
    private Firebase ref;
    private ListView listCentre;
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationNewOverlay;
   private RadiusMarkerClusterer radiusMarkerClusterer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**
         * Contecem amb firebase.
         */
        Firebase.setAndroidContext(getBaseContext());
        ref = new Firebase("https://testgimmapp.firebaseio.com/");
        infoGymRef = ref.child("InfoGym");
        /**
         * Objectes layout.
         */

        listCentre = (ListView)findViewById(R.id.listCentre);
        map = (MapView) findViewById(R.id.map);

        /**
         * Configuració de diferents element.
         */
        configuraciodelMapa();
        posicionsGimnas();
        configLlistatGimnas();

    }

    /**
     * Metode per configurar el llistat de gimnasos.
     */
    private void configLlistatGimnas(){

        FirebaseListAdapter adapter = new FirebaseListAdapter<InfoGym>(this, InfoGym.class, R.layout.list_localiza_centro, infoGymRef) {
            @Override
            protected void populateView(View v, InfoGym info, int position) {
                TextView nom = (TextView) v.findViewById(R.id.nomCentre);
                TextView direccio = (TextView) v.findViewById(R.id.ubicacioCentre);
                TextView email = (TextView) v.findViewById(R.id.emailCentre);
                TextView telefon = (TextView) v.findViewById(R.id.telfCentre);
                TextView semana = (TextView) v.findViewById(R.id.horariCentre);
                TextView horari2 = (TextView)v.findViewById(R.id.horari2);
                TextView horari3 = (TextView)v.findViewById(R.id.horari3);


                nom.setText(info.getNombreGym());
                direccio.setText(info.direccionGym);
                email.setText(info.getCorreoElectronicoGym());
                telefon.setText(String.valueOf(info.getTelefonoGym()));
                semana.setText("Lunes- viernes : " + info.getHorarioGym()[0]);
                horari2.setText("Sabados : " + info.getHorarioGym()[1]);
                horari3.setText("Domingos y festivos : " + info.getHorarioGym()[2]);

            }
        };
        listCentre.setAdapter(adapter);
    }
    /**
     * Metode que extreu del firebase totes les posicions dels bolets i les col·loca en el mapa
     */

    private void posicionsGimnas()
    {
        radiusMarkerClusterer = new RadiusMarkerClusterer(getBaseContext());
        map.getOverlays().add(radiusMarkerClusterer);
        radiusMarkerClusterer.setRadius(100);


        infoGymRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    InfoGym ubicacio = postSnapshot.getValue(InfoGym .class);
                    Marker gimnasPoint = new Marker(map);
                    GeoPoint point = new GeoPoint(ubicacio.getLatitudGym(), ubicacio.getLongitudGym());
                    //Afegueixo icono personalitzada als "marker" del mapa.
                    gimnasPoint.setIcon(getResources().getDrawable(R.drawable.ic_room_indigo_a400_24dp));
                    gimnasPoint.setPosition(point);
                    gimnasPoint.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    //Afegueixo titol
                    gimnasPoint.setTitle(ubicacio.getNombreGym());
                    //Afegueixo un "retall"
                   gimnasPoint.setSnippet(ubicacio.direccionGym);
                    //Afegueixo sub-descripcio
                    gimnasPoint.setSubDescription(String.valueOf(ubicacio.getTelefonoGym()));
                    gimnasPoint.setAlpha(0.6f);
                    radiusMarkerClusterer.add(gimnasPoint);
                }
                radiusMarkerClusterer.invalidate();
                map.invalidate();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Configuració inicial del mapa.
     */
    private void configuraciodelMapa()
    {
       map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setTilesScaledToDpi(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        myLocationNewOverlay = new MyLocationNewOverlay(getBaseContext(), new GpsMyLocationProvider(getBaseContext()), map);
        myLocationNewOverlay.enableMyLocation();

        myLocationNewOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapController.animateTo(myLocationNewOverlay
                        .getMyLocation());
            }
        });

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(displayMetrics.widthPixels / 2, 10);
        CompassOverlay compassOverlay = new CompassOverlay(getBaseContext(), new InternalCompassOrientationProvider(getBaseContext()), map);
        compassOverlay.enableCompass();
        map.getOverlays().add(myLocationNewOverlay);
        map.getOverlays().add(scaleBarOverlay);
        map.getOverlays().add(compassOverlay);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
