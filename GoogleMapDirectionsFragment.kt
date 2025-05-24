package apps.srichaitanya.mis.fragments.googleMap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apps.srichaitanya.employee.model.campusSearch.BuildingListResposneDirectionItem
import apps.srichaitanya.mis.R
import apps.srichaitanya.mis.adapters.CheckboxTextAdapter
import apps.srichaitanya.mis.adapters.CustomSpinner
import apps.srichaitanya.mis.adapters.GoogleMapBottomSheetImageRecyclerAdapter
import apps.srichaitanya.mis.adapters.SelectedBuidlingImagesAdapter
import apps.srichaitanya.mis.databinding.FragmentGoogleMapDirectionsBinding
import apps.srichaitanya.mis.model.MapMarkerModel
import apps.srichaitanya.mis.model.MapMarkerModel1
import apps.srichaitanya.mis.model.campusstaff.GoogleMapRequest
import apps.srichaitanya.mis.model.googleMapDirections.BuildingLeaseDetailsBodyRequest
import apps.srichaitanya.mis.model.googleMapDirections.BuildingLeaseDetailsReposneGoogleMapItem
import apps.srichaitanya.mis.model.googleMapDirections.CampusType
import apps.srichaitanya.mis.model.googleMapDirections.LosBuildingListRequestResponse
import apps.srichaitanya.mis.model.googleMapDirections.LosBuildingListResponseItem
import apps.srichaitanya.mis.model.googleMapDirections.LosType
import apps.srichaitanya.mis.model.googleMapDirections.MyCanvasView
import apps.srichaitanya.mis.model.googleMapDirections.SubZone
import apps.srichaitanya.mis.model.googleMapDirections.Zone
import apps.srichaitanya.mis.utils.EmployeeSharedPreference
import apps.srichaitanya.mis.utils.Status
import apps.srichaitanya.mis.viewmodels.CampusStaffViewModel
import apps.srichaitanya.mis.viewmodels.DirectionsViewModels
import apps.srichaitanya.mis.viewmodels.EmployeeDashboardViewModel
import apps.srichaitanya.mis.viewmodels.GoogleMapDirectionsViewModels
import apps.srichaitanya.mis.viewmodels.ScaitsMisReportViewModels
import com.devs.mdmanager.MapDrawingManager
import com.devs.mdmanager.OnShapeDrawListener
import com.devs.mdmanager.OnShapeRemoveListener
import com.devs.mdmanager.ShapeType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.model.TravelMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.Field
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt


@AndroidEntryPoint
class GoogleMapDirectionsFragment : Fragment(), OnMapReadyCallback,
    OnShapeDrawListener,
    OnShapeRemoveListener {
    private lateinit var mBinding: FragmentGoogleMapDirectionsBinding
    private lateinit var mMap: GoogleMap
    lateinit var campus: LatLng
    var id: String? = null
    var businessType: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var buildingName: String? = null
    var buildingId: String? = null
    var stateName: String? = null
    var cityName: String? = null
    var zone: String? = null
    var subZone: String? = null
    var campusName: String? = null
    var address: String? = null
    var sqFtBui: Int? = 0
    var otherSqft: Int? = 0
    var totalSqft: Int? = 0
    var rentBui: Int? = 0
    var gst: Int? = 0
    var rentWithGst: Int? = 0
    var rentWithGstYr: Int? = 0
    var strn: Int? = 0
    var advance: Int? = 0
    var estimatedStrn: Int? = 0
    var avgSqFtStu: Double? = 0.0
    var avgRentSqFt: Double? = 0.0
    private val campusStaffViewModel: CampusStaffViewModel by viewModels()
    private val buildingDetailsViewModel: EmployeeDashboardViewModel by viewModels()
    private var mMarkerPoints: ArrayList<MapMarkerModel> = arrayListOf()
    private var mMarkerPoints1: ArrayList<MapMarkerModel1> = arrayListOf()

    private var googleMarkerPoints: ArrayList<BuildingListResposneDirectionItem> = arrayListOf()

//    private var dataList: ArrayList<BuildingListBasedOnCampusIDResposneItem>,

    private var googleMapAdapter: GoogleMapBottomSheetImageRecyclerAdapter? = null
    private lateinit var map: MapView
    private lateinit var markerName: Marker
    private val markers = mutableListOf<LatLng>()
    private val markerList: MutableList<Marker> = mutableListOf()

    private val textViewArrayList = ArrayList<TextView>()
    private val geoApiContext: GeoApiContext by lazy {
        GeoApiContext.Builder().apiKey(getString(R.string.api_key1)).build()
    }
    private val directionsViewModels: DirectionsViewModels by activityViewModels()
    private val scaitsMisReportViewModels: ScaitsMisReportViewModels by viewModels()

    private var sourceLatLng: LatLng? = null

    private var destinationLatLng: LatLng? = null

    private lateinit var distanceTextView: TextView

    private var bottomSheetmap2: ConstraintLayout? = null
    private var bottomSheetBehaviormap2: BottomSheetBehavior<View>? = null


    private var bottomSheetmap2icon: ConstraintLayout? = null
    private var bottomSheetBehaviormap2icon: BottomSheetBehavior<View>? = null

    var googleMap: String? = null
    var isDrawingEnabled = false

    private var polylinePoints = mutableListOf<LatLng>()
    private var currentPolyline: Polyline? = null


    val listLat: ArrayList<LatLng> = arrayListOf()
    var losData: ArrayList<LosType> = arrayListOf()
    var campusData: ArrayList<CampusType> = arrayListOf()
    var zonesData: ArrayList<Zone> = arrayListOf()
    var subZonesData: ArrayList<SubZone> = arrayListOf()
    private lateinit var layerLayout: FrameLayout
    private lateinit var myCanvasView: MyCanvasView
    val uniqueIdsSelectedBuilding: ArrayList<Any> = ArrayList()
    var stringList: ArrayList<String> = ArrayList()
    private var mandalValueList = arrayListOf<String>()
    private var mandalAdapter: CustomSpinner? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::mBinding.isInitialized.not()) {

            mBinding = FragmentGoogleMapDirectionsBinding.inflate(inflater, container, false)
            mBinding.backButton.setOnClickListener {
                Navigation.findNavController(requireView()).popBackStack()
            }

            val callback: OnBackPressedCallback =
                object : OnBackPressedCallback(true /* enabled by default */) {
                    override fun handleOnBackPressed() {
                        // Handle the back button even
                    }
                }

            requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

            bottomSheetmap2 = view?.findViewById(R.id.distanceSheet)
            bottomSheetBehaviormap2 = bottomSheetmap2?.let { BottomSheetBehavior.from(it) }

            bottomSheetmap2icon = view?.findViewById(R.id.iconSheet)
            bottomSheetBehaviormap2icon = bottomSheetmap2icon?.let { BottomSheetBehavior.from(it) }

            if (bottomSheetmap2 != null && bottomSheetmap2icon != null) {
                val params = bottomSheetmap2?.layoutParams as? CoordinatorLayout.LayoutParams
                if (params != null) {
                    params.anchorId = bottomSheetmap2icon?.id!!
                    params.anchorGravity = Gravity.TOP
                    bottomSheetmap2?.layoutParams = params
                }

                bottomSheetmap2?.visibility = View.VISIBLE
            }

            mBinding.iconSheet.directionsIconLayout.setOnClickListener {
                mBinding.cardView2.visibility = View.VISIBLE
                mMap.uiSettings.isScrollGesturesEnabled = true
                mBinding.distanceSheet.sheet1.visibility = View.VISIBLE
                mBinding.cardView.visibility = View.GONE
                mBinding.filterIcon.visibility=View.GONE
            }
            mBinding.iconSheet.homeIconLayout.setOnClickListener {
                mBinding.cardView2.visibility = View.GONE
                mBinding.distanceSheet.sheet1.visibility = View.GONE
                mBinding.cardView.visibility = View.VISIBLE
                mBinding.filterIcon.visibility=View.VISIBLE

            }

            mBinding.cardView.setOnClickListener {


            }

            mBinding.drawButton.setOnClickListener {
                isDrawingEnabled = true
//                mBinding.cardView.visibility = View.VISIBLE
                mBinding.drawButton.visibility = View.GONE
                mBinding.applyCancelLayout.visibility = View.VISIBLE
                mBinding.applyButton.visibility = View.VISIBLE
                mBinding.cancelButton.visibility = View.VISIBLE
                mBinding.layerLayout.visibility = View.VISIBLE


//                mBinding.boundarysButton.visibility=View.GONE
//                mBinding.applyCancelLayout.visibility = View.GONE
//                mBinding.cardView.visibility = View.VISIBLE


                mMap.uiSettings.isScrollGesturesEnabled = false
                showMapLayers(mMap)
            }

            mBinding.applyButton.setOnClickListener {
                isDrawingEnabled = false

                if (listLat.isEmpty()) {
                    myCanvasView.clearCanvas()
                    listLat.clear() // Clear any partially drawn points if necessary
                    myCanvasView.clearCanvas()
                    mMap.clear()
//                mBinding.cardView.visibility = View.VISIBLE
                    mBinding.drawButton.visibility = View.VISIBLE
                    mBinding.applyCancelLayout.visibility = View.GONE

                    mBinding.layerLayout.visibility = View.GONE

                    mMap.uiSettings.isScrollGesturesEnabled = true

                    addGoogleMarkers()

                    Toast.makeText(
                        requireContext(),
                        "Draw Boundary Where ever u want on map",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    mBinding.applyButton.visibility = View.GONE
                    mBinding.cancelButton.visibility = View.GONE
                    mBinding.boundarysButton.visibility = View.VISIBLE
                    mBinding.viewallButton.visibility = View.VISIBLE

                    mBinding.layerLayout.visibility = View.GONE

                    clearMarkers()
                    mMap.uiSettings.isScrollGesturesEnabled = true

                    comparePoints(googleMarkerPoints, listLat)
                }

            }
            mBinding.boundarysButton.setOnClickListener {
                isDrawingEnabled = false

                mBinding.boundarysButton.visibility = View.GONE
                mBinding.viewallButton.visibility = View.GONE

                mBinding.applyCancelLayout.visibility = View.GONE
//                mBinding.cardView.visibility = View.VISIBLE
                mBinding.drawButton.visibility = View.VISIBLE
                mBinding.layerLayout.visibility = View.GONE
                listLat.clear()
                myCanvasView.clearCanvas()
                mMap.clear()
                mMap.uiSettings.isScrollGesturesEnabled = true
                addGoogleMarkers()
                uniqueIdsSelectedBuilding.clear()

            }

            mBinding.cancelButton.setOnClickListener {
                isDrawingEnabled = false
                listLat.clear() // Clear any partially drawn points if necessary
                myCanvasView.clearCanvas()
                mMap.clear()
//                mBinding.cardView.visibility = View.VISIBLE
                mBinding.drawButton.visibility = View.VISIBLE
                mBinding.applyCancelLayout.visibility = View.GONE

                mBinding.layerLayout.visibility = View.GONE

                mMap.uiSettings.isScrollGesturesEnabled = true

                addGoogleMarkers()

            }






            addDots(3)

            id = arguments?.getString("id")
            businessType = arguments?.getString("businessType")
//        latitude = arguments?.getString("latitude")
//        longitude = arguments?.getString("longitude")
            buildingName = arguments?.getString("buildingName")
            buildingId = arguments?.getString("buildingId")
            stateName = arguments?.getString("stateName")
            cityName = arguments?.getString("cityName")
            zone = arguments?.getString("zone")
            address = arguments?.getString("address")
            subZone = arguments?.getString("subZone")
            campusName = arguments?.getString("campusName")
            sqFtBui = arguments?.getInt("sqFtBui")
            otherSqft = arguments?.getInt("otherSqft")
            totalSqft = arguments?.getInt("totalSqft")
            rentBui = arguments?.getInt("rentBui")
            gst = arguments?.getInt("gst")
            rentWithGst = arguments?.getInt("rentWithGst")
            rentWithGstYr = arguments?.getInt("rentWithGstYr")
            strn = arguments?.getInt("strn")
            advance = arguments?.getInt("advance")
            estimatedStrn = arguments?.getInt("estimatedStrn")
            avgSqFtStu = arguments?.getDouble("avgSqFtStu")
            avgRentSqFt = arguments?.getDouble("avgRentSqFt")
            googleMap = arguments?.getString("googleMap")

            mBinding.viewallButton.setOnClickListener {

                // Convert to ArrayList<String> if possible
                stringList = uniqueIdsSelectedBuilding.mapNotNull { it.toString() } as ArrayList<String>

                val bundle = Bundle().apply {
                    putStringArrayList("uniqueIdsSelectedBuilding", stringList)
                    putString("businessType", businessType)
                }
                Log.d("TAG", "uniqueIdsSelectedBuilding:$stringList ")

                findNavController().navigate(
                    R.id.action_google_maps_to_selectedCampusListScreenGoogleMap,
                    bundle
                )
            }
            Log.d("TAG", "buildingIdTest:$buildingId ")
//        if (googleMap=="Google"){
            getGoogleMapLocationsResponse()
            losFiltersListResponse()



            mBinding.filterIcon.setOnClickListener {
//                val popupOptions = ArrayList(listOfNotNull(
//                    "LOS",
//                    "Campus List",
//                    "Zones ",
//                    "Sub Zones"
//                ))
//
//                // Show popup with dynamic text
//                showPopupMenu(popupOptions)
                BottomSheetForList()
            }

//        }
//        mMarkerPoints.add(
//            MapMarkerModel(
//                latitude,
//                longitude,
//                buildingName,
//                buildingId?.toInt()
//            )
//        )


//        mMarkerPoints1.add(
//            MapMarkerModel1(
//                latitude,
//                longitude,
//                buildingName,
//                buildingId?.toInt(),
//                false,
//                campusName,
//                sqFtBui,
//                otherSqft,
//                totalSqft,
//                rentBui,
//                gst,
//                rentWithGst,
//                rentWithGstYr,
//                strn,
//                avgSqFtStu,
//                avgRentSqFt,
//                advance,
//                estimatedStrn,
//                address,
//                stateName,
//                cityName,
//                zone,
//                subZone
//            )
//        )
//        mBinding.sourceValue.text = buildingName
            layerLayout = mBinding.layerLayout


            MapsInitializer.initialize(requireContext())
            map = mBinding.map
            map.onCreate(savedInstanceState)
            map.getMapAsync(this)

            mBinding.destinationLayout.setOnClickListener {
                findNavController().navigate(R.id.action_google_maps_to_GoogleMapCampusSearchDirections)
            }

        }

        return mBinding.root
    }

    private fun clearMarkers() {
        for (marker in markerList) {
            marker.remove()
        }
        markerList.clear()


    }

    private fun addGoogleMarkers() {
        for (mData in googleMarkerPoints) {
            val markerView = LayoutInflater.from(requireContext()).inflate(
                R.layout.location_marker_design, null
            )
            val cardView = markerView.findViewById<ConstraintLayout>(R.id.card)
            val itemText = markerView.findViewById<AppCompatTextView>(R.id.location_name)

            itemText.visibility = View.INVISIBLE
            itemText.text = mData.buildingName
            itemText.setBackgroundResource(R.color.white)

            val bitmap1 = Bitmap.createScaledBitmap(
                viewToBitmap(cardView)!!,
                cardView.width,
                cardView.height,
                false
            )
            val smallMaker = BitmapDescriptorFactory.fromBitmap(bitmap1)

            val marker1 = mMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            mData.latitude?.toDouble() ?: 0.0,
                            mData.longitude?.toDouble() ?: 0.0
                        )
                    )
                    .icon(smallMaker)
            )
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12F), 1000, null)

            marker1?.tag = mData.buildingId

            val latlng = LatLng(
                mData.latitude?.toDouble() ?: 0.0,
                mData.longitude?.toDouble() ?: 0.0
            )
            markers.add(latlng)
            marker1?.let { markerList.add(it) }

            if (googleMarkerPoints.indexOf(mData) == 0) {
                mBinding.sourceValue.text = mData.buildingName
                buildingName = mData.buildingName
                latitude = mData.latitude
                longitude = mData.longitude

            }
        }
    }


//    private fun getPolygonCenterPoint(polygonPointsList: ArrayList<LatLng>): LatLng {
//        var centerLatLng: LatLng? = null
//        val builder: LatLngBounds.Builder = LatLngBounds.Builder()
//        for (i in polygonPointsList.indices) {
//            builder.include(polygonPointsList[i])
//        }
//        val bounds: LatLngBounds = builder.build()
//        centerLatLng = bounds.center
//        calculateRadius(centerLatLng.latitude,centerLatLng.longitude,listLat)
//        Log.d("TAG", "getPolygonCenterPoint:$centerLatLng")
//        return centerLatLng
//    }
//    fun findPointsWithinRadius(
//        centerLat: Double,
//        centerLng: Double,
//        radius: Double,
//        allPoints: List<LatLng>
//    ): List<LatLng> {
//        val pointsWithinRadius: ArrayList<LatLng> = ArrayList()
//        for (point in allPoints) {
//            val distance: Double = haversine(centerLat, centerLng, point.latitude, point.longitude)
//            if (distance <= radius) {
//                pointsWithinRadius.add(point)
//            }
//        }
//        Log.d("TAG", "findPointsWithinRadius: $pointsWithinRadius")
////        completeDrawing(pointsWithinRadius)
//
//
//        return pointsWithinRadius
//    }
//    fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
//        val R = 6371000 // Radius of the earth in meters
//        val latDistance = Math.toRadians(lat2 - lat1)
//        val lonDistance = Math.toRadians(lng2 - lng1)
//        val a = sin(latDistance / 2) * sin(latDistance / 2) + cos(
//            Math.toRadians(lat1)
//        ) * cos(Math.toRadians(lat2)) * sin(lonDistance / 2) * sin(
//            lonDistance / 2
//        )
//        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//        return R * c // Distance in meters
//    }
//
//    fun calculateRadius(centerLat: Double, centerLng: Double, coordinates: ArrayList<LatLng>): Float {
//        var maxDistance = 0f
//
//        // Center location
//        val centerLocation = Location("").apply {
//            latitude = centerLat
//            longitude = centerLng
//        }
//
//        // Iterate through all points
//        for (coordinate in coordinates) {
//            val pointLocation = Location("").apply {
//                latitude = coordinate.latitude
//                longitude = coordinate.longitude
//            }
//
//            // Calculate distance between center and the current point
//            val distance = centerLocation.distanceTo(pointLocation)
//            if (distance > maxDistance) {
//                maxDistance = distance
//            }
//        }
//        Log.d("TAG", "calculateRadius:$maxDistance")
//        findPointsWithinRadius(centerLat,centerLng,maxDistance.toDouble(),listLat)
//
//        return maxDistance // Radius in meters
//    }
//    private fun completeDrawing(List :ArrayList<LatLng>) {
//        Log.d("TAG", "completeDrawinglistLat:$List")
//        Log.d("TAG", "completeDrawinggoogleMarkerPoints:$googleMarkerPoints")
//
//        val precision = 6 // Set the precision to 6 decimal places
//        var common: ArrayList<BuildingListResposneDirectionItem> = arrayListOf()
//        googleMarkerPoints.forEach {
//
//            List.forEach{item->
//                 val roundedLat = "%.6f".format(item.latitude.toDouble())  // Convert to Double if necessary
//                 val roundedLng = "%.6f".format(item.longitude.toDouble())  // Convert to Double if necessary
//
//                    if (it.latitude.equals(roundedLat) && it.longitude.equals(roundedLng))
//                    {
//                    common.add(it)
//                }
//            }
//        }
//
////        for (latLng1 in googleMarkerPoints) {
//            // Assuming latLng1 contains data with latitude, longitude, and buildingId
////            val matchedMarker = List.find { googleMarker ->
////                // Ensure googleMarker.latitude and googleMarker.longitude are Doubles
////                val roundedLat = "%.6f".format(googleMarker.latitude.toDouble())  // Convert to Double if necessary
////                val roundedLng = "%.6f".format(googleMarker.longitude.toDouble()) // Convert to Double if necessary
////
////                // Ensure latLng1.latitude and latLng1.longitude are Doubles
//////                val roundedLatLng1 = "%.6f".format(latLng1.latitude.toDouble())
//////                val roundedLngLatLng1 = "%.6f".format(latLng1.longitude.toDouble())
////
////                // Compare rounded values
////                roundedLat == roundedLatLng1 && roundedLng == roundedLngLatLng1
////            }
//
//            Log.d("TAG", "completeDrawing:$common")
//
//            common.let { it ->
//                it.forEach { googleMarker->
//
//                    val markerView =
//                        (requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)!! as LayoutInflater).inflate(
//                            apps.srichaitanya.mis.R.layout.location_marker_design_yellow,
//                            null
//                        )
//                    val cardView = markerView.findViewById<ConstraintLayout>(apps.srichaitanya.mis.R.id.card)
//                    val itemText: AppCompatTextView = markerView.findViewById(apps.srichaitanya.mis.R.id.location_name)
//                    itemText.visibility = View.INVISIBLE
//
//                    val bitmap = viewToBitmap(cardView)?.let {
//                        Bitmap.createScaledBitmap(it, cardView.width, cardView.height, false)
//                    }
//
//                    // Convert bitmap to BitmapDescriptor
//                    val smallMaker = bitmap?.let {
//                        BitmapDescriptorFactory.fromBitmap(it)
//                    }
//
//                    // Add the marker to the map with the matching latLng position
//                    val newMarker = mMap.addMarker(
//                        MarkerOptions()
//                            .position(LatLng(googleMarker.latitude.toDouble(), googleMarker.longitude.toDouble()))
//                            .icon(smallMaker)
//                    )
//
//                    // Optionally add data to the marker for future use (e.g., buildingId)
//                    // Ensure latLng1 has a buildingId or relevant data to be assigned as tag
////                    newMarker?.tag = latLng1.buildingId
//
//                }
//                // Inflate the marker view
//               // Ensure latLng1 has a buildingId property
//            }
////        }
//    }


    private fun getGoogleMapLocationsResponse() {

        scaitsMisReportViewModels.getBuildingListResponse()
            .observe(viewLifecycleOwner) {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            progressVisibility(false)
                            googleMarkerPoints = resource.data!!

                            if (googleMarkerPoints.isNullOrEmpty()) {
                                mBinding.noData.noDataLayout.visibility = View.VISIBLE
                                mBinding.map.visibility = View.GONE
                                mBinding.cardView2.visibility = View.GONE
                                mBinding.distanceSheet.sheet1.visibility = View.GONE
//                                mBinding.mapModeIcon.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    "Latitude and Longitude Null data Found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                mBinding.noData.noDataLayout.visibility = View.GONE
//                                mBinding.distanceSheet.sheet1.visibility = View.VISIBLE
                                mBinding.map.visibility = View.VISIBLE
//                                mBinding.cardView2.visibility = View.VISIBLE
                                mBinding.mapModeIcon.visibility = View.VISIBLE
                                mMap.let { onMapReady(it) }
                            }


                        }

                        Status.LOADING -> {
                            progressVisibility(true)
                        }

                        else -> {
                            progressVisibility(false)
                        }
                    }
                }
            }


    }
    private fun losFiltersListResponse() {
        scaitsMisReportViewModels.losFiltersListResponse()
            .observe(viewLifecycleOwner) { resource ->
                resource?.let {
                    when (resource.status) {
                        Status.SUCCESS -> {
                            progressVisibility(false)
                            val losResponses = resource.data!!

                            // Extract values dynamically
                            losData = losResponses.losTypes
                             campusData = losResponses.campusTypes
                            zonesData = losResponses.zones
                             subZonesData = losResponses.subZones


                        }

                        Status.LOADING -> {
                            progressVisibility(true)
                        }

                        else -> {
                            progressVisibility(false)
                        }
                    }
                }
            }
    }

    private fun showPopupMenu(options: ArrayList<String>) {
            val inflater = LayoutInflater.from(requireContext())
            val popupView = inflater.inflate(R.layout.filter_list_design_maps, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            popupView.elevation = 14f
            popupWindow.isOutsideTouchable = true

            // Find the LinearLayout where dynamic items will be added
            val menuContainer = popupView.findViewById<LinearLayout>(R.id.popup_container)
            menuContainer.removeAllViews() // Clear previous views

            // Dynamically create TextViews for each option
            options.forEach { optionText ->
                val textView = TextView(requireContext()).apply {
                    text = optionText
                    textSize = 16f
                    setPadding(20, 20, 20, 20)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    isClickable = true
                    isFocusable = true
                    gravity = Gravity.CENTER
                }

                // Handle clicks
                textView.setOnClickListener {
                    popupWindow.dismiss()
                    if (optionText == "LOS List") {
                        // Show bottom sheet with LOS list
//                        val losBottomSheet = LosBottomSheetDialog(losList)
//                        losBottomSheet.show(parentFragmentManager, "LosBottomSheetDialog")
                    } else {
                        Toast.makeText(requireContext(), optionText, Toast.LENGTH_SHORT).show()
                    }                }

                menuContainer.addView(textView) // Add TextView to the layout
            }

            popupWindow.showAsDropDown(mBinding.filterIcon, 0, 0)
//        }
    }


    private fun BottomSheetForList() {
        val dialog = BottomSheetDialog(
            requireContext(),
            R.style.BaseBottomSheetDialog1
        )
        val bottomSheetView = layoutInflater.inflate(R.layout.typelist_bottom_sheet_maps, null)

        val closeIcon: AppCompatImageView = bottomSheetView.findViewById(R.id.close_dialog)
        val recyclerView: RecyclerView = bottomSheetView.findViewById(R.id.typeSelected_recycler)
        val losButton: AppCompatTextView = bottomSheetView.findViewById(R.id.los_button)
        val campusTypeButton: AppCompatTextView = bottomSheetView.findViewById(R.id.campusType_button)
        val zoneButton: AppCompatTextView = bottomSheetView.findViewById(R.id.zone_button)
        val subZoneButton: AppCompatTextView = bottomSheetView.findViewById(R.id.subzone_button)
        val fabClick: FloatingActionButton = bottomSheetView.findViewById(R.id.fab)

        var selectedTab = "los"

        fun updateButtonStyles(selectedTab: String) {
            losButton.setBackgroundResource(if (selectedTab == "los") R.drawable.button_selected else R.drawable.button_unselected)
            campusTypeButton.setBackgroundResource(if (selectedTab == "campusType") R.drawable.button_selected else R.drawable.button_unselected)
            zoneButton.setBackgroundResource(if (selectedTab == "zone") R.drawable.button_selected else R.drawable.button_unselected)
            subZoneButton.setBackgroundResource(if (selectedTab == "subzone") R.drawable.button_selected else R.drawable.button_unselected)

            losButton.setTextColor(if (selectedTab == "los") resources.getColor(R.color.circle_bg_colour) else resources.getColor(R.color.black))
            campusTypeButton.setTextColor(if (selectedTab == "campusType") resources.getColor(R.color.circle_bg_colour) else resources.getColor(R.color.black))
            zoneButton.setTextColor(if (selectedTab == "zone") resources.getColor(R.color.circle_bg_colour) else resources.getColor(R.color.black))
            subZoneButton.setTextColor(if (selectedTab == "subzone") resources.getColor(R.color.circle_bg_colour) else resources.getColor(R.color.black))
        }


        fun mapDataToList(selectedTab: String): ArrayList<Pair<String, String>> {
            return when (selectedTab) {
                "los" -> ArrayList(losData.map { it.los to it.uniqueRefId })
                "campusType" -> ArrayList(campusData.map { it.campusType to it.uniqueRefId })
                "zone" -> ArrayList(zonesData.map { it.zone to it.uniqueRefId })
                "subzone" -> ArrayList(subZonesData.map { it.subZone to it.uniqueRefId })
                else -> arrayListOf()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CheckboxTextAdapter(mapDataToList(selectedTab))
        recyclerView.adapter = adapter

        fun updateRecyclerView(selectedTab: String) {
            adapter.updateData(mapDataToList(selectedTab))
        }



            fabClick.setOnClickListener {
                val selectedItems = adapter.getSelectedItems()

                val requestBody = LosBuildingListRequestResponse(
                    losTypes = if (selectedTab == "los") selectedItems else arrayListOf(),
                    campusTypes = if (selectedTab == "campusType") selectedItems else arrayListOf(),
                    zones = if (selectedTab == "zone") selectedItems else arrayListOf(),
                    subZones = if (selectedTab == "subzone") selectedItems else arrayListOf()
                )
                mMap.clear()
                losFilterBuildingListResponse(requestBody) // Call API with selected data
                dialog.dismiss()
            }


        losButton.setOnClickListener {
            selectedTab = "los"
            updateButtonStyles(selectedTab)
            updateRecyclerView(selectedTab)

        }
        campusTypeButton.setOnClickListener {
            selectedTab = "campusType"
            updateButtonStyles(selectedTab)
            updateRecyclerView(selectedTab)

        }
        zoneButton.setOnClickListener {
            selectedTab = "zone"
            updateButtonStyles(selectedTab)
            updateRecyclerView(selectedTab)

        }
        subZoneButton.setOnClickListener {
            selectedTab = "subzone"
            updateButtonStyles(selectedTab)
            updateRecyclerView(selectedTab)

        }

        updateButtonStyles(selectedTab)
        updateRecyclerView(selectedTab)


        closeIcon.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.setContentView(bottomSheetView)
        dialog.show()
    }
    private fun losFilterBuildingListResponse(requestResponse: LosBuildingListRequestResponse){
        scaitsMisReportViewModels.losFilterBuildingListResponse(requestResponse)
            .observe(viewLifecycleOwner) {
                it.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            progressVisibility(false)

                         var   losBuildingListResponseItem =resource.data ?: ArrayList<LosBuildingListResponseItem>()

                            Log.d("TAG", "losFilterBuildingListResponse: $losBuildingListResponseItem")

                            if (losBuildingListResponseItem.isNullOrEmpty()) {
//                                mBinding.listSelectedRecycler.visibility=View.GONE
                                mBinding.noData.noDataLayout.visibility=View.VISIBLE
                                mBinding.map.visibility=View.GONE

//                                noImages.visibility = View.VISIBLE
//                                buildingImagesRecycler.visibility = View.GONE
                            } else {
                                mBinding.map.visibility=View.VISIBLE
                                mBinding.noData.noDataLayout.visibility=View.GONE


                                losBuildingListResponseItem.forEachIndexed { index, mData ->
                                    val markerView = LayoutInflater.from(requireContext()).inflate(
                                        R.layout.location_marker_design, null
                                    )
                                    val cardView = markerView.findViewById<ConstraintLayout>(R.id.card)
                                    val itemText = markerView.findViewById<AppCompatTextView>(R.id.location_name)

                                    itemText.visibility = View.INVISIBLE
                                    itemText.text = mData.buildingName
                                    itemText.setBackgroundResource(R.color.white)

                                    val bitmap1 = Bitmap.createScaledBitmap(
                                        viewToBitmap(cardView)!!,
                                        cardView.width,
                                        cardView.height,
                                        false
                                    )
                                    val smallMaker = BitmapDescriptorFactory.fromBitmap(bitmap1)

                                    val marker1 = mMap.addMarker(
                                        MarkerOptions()
                                            .position(
                                                LatLng(
                                                    mData.latitude?.toDouble() ?: 0.0,
                                                    mData.longitude?.toDouble() ?: 0.0
                                                )
                                            )
                                            .icon(smallMaker)
                                    )

                                    marker1?.tag = mData.buildingId

                                    val latlng = LatLng(
                                        mData.latitude?.toDouble() ?: 0.0,
                                        mData.longitude?.toDouble() ?: 0.0
                                    )
                                    markers.add(latlng)
                                    marker1?.let { markerList.add(it) }

                                    // Move camera to first marker only
                                    if (index == 0) {
                                        mBinding.sourceValue.text = mData.buildingName
                                        buildingName = mData.buildingName
                                        latitude = mData.latitude
                                        longitude = mData.longitude

                                        mMap.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(latlng, 16F),
                                            1000,
                                            null
                                        )
                                    }
                                }

                            }
                        }

                        Status.ERROR -> {
//                            progressVisibility(false)

                        }

                        Status.LOADING -> {
//                            progressVisibility(true)
                        }

                        Status.NETWORK_ERROR -> {
//                            progressVisibility(false)

                        }

                        else -> {}
                    }
                }
            }
    }

    private fun comparePoints(
        apiPoints: ArrayList<BuildingListResposneDirectionItem>,
        polygon: ArrayList<LatLng>
    ) {
        val matchedPoints = apiPoints.filter { apiPoint ->
            isPointInPolygon(
                LatLng(apiPoint.latitude.toDouble(), apiPoint.longitude.toDouble()),
                polygon
            )
        }

        if (matchedPoints.isEmpty()) {
            Log.d("TAG", "No matched points found.")
            return
        }

        val boundsBuilder = LatLngBounds.Builder()

        matchedPoints.forEach { point ->
            val markerView =
                (requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    apps.srichaitanya.mis.R.layout.location_marker_design_yellow,
                    null
                )
            val cardView =
                markerView.findViewById<ConstraintLayout>(apps.srichaitanya.mis.R.id.card)
            val itemText: AppCompatTextView =
                markerView.findViewById(apps.srichaitanya.mis.R.id.location_name)
            itemText.visibility = View.INVISIBLE
            val bitmap = viewToBitmap(cardView)?.let {
                Bitmap.createScaledBitmap(it, cardView.width, cardView.height, false)
            }

            val smallMarker = bitmap?.let {
                BitmapDescriptorFactory.fromBitmap(it)
            }

            val markerPosition = LatLng(
                point.latitude.toDouble(),
                point.longitude.toDouble()
            )

            val newMarker = mMap.addMarker(
                MarkerOptions()
                    .position(markerPosition)
                    .icon(smallMarker)
            )

            newMarker?.tag = point.buildingId
            boundsBuilder.include(markerPosition) 

            point.buildingId.let {
                uniqueIdsSelectedBuilding.add(it)
            }
        }

        Log.d("MarkerTags", "Current marker tags: $uniqueIdsSelectedBuilding")
        Log.d("TAG", "Matched Points: $matchedPoints")

        val bounds = boundsBuilder.build()

        val centerLatLng = LatLng(
            (bounds.southwest.latitude + bounds.northeast.latitude) / 2,
            (bounds.southwest.longitude + bounds.northeast.longitude) / 2
        )

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 12f))
    }




    private fun isPointInPolygon(point: LatLng, polygon: ArrayList<LatLng>): Boolean {
        var intersects = 0
        for (i in polygon.indices) {
            val p1 = polygon[i]
            val p2 = polygon[(i + 1) % polygon.size]
            if ((point.latitude > Math.min(p1.latitude, p2.latitude)) &&
                (point.latitude <= Math.max(p1.latitude, p2.latitude)) &&
                (point.longitude <= Math.max(p1.longitude, p2.longitude)) &&
                (p1.latitude != p2.latitude)
            ) {
                val xinters =
                    (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) / (p2.latitude - p1.latitude) + p1.longitude
                if (p1.longitude == p2.longitude || point.longitude <= xinters) {
                    intersects++
                }
            }
        }
        return intersects % 2 != 0
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun showMapLayers(map: GoogleMap) {
        myCanvasView = MyCanvasView(requireContext())

        layerLayout.addView(
            myCanvasView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        myCanvasView.setOnTouchListener { _, event ->
            if (!isDrawingEnabled) return@setOnTouchListener false

            val x = event.x
            val y = event.y
            val projection = map.projection
            val latLng = projection.fromScreenLocation(Point(x.roundToInt(), y.roundToInt()))

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("TAG", "Touch down at: $latLng")
                    listLat.add(latLng)
//                    Draw_Map()
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.d("TAG", "Touch move at: $latLng")
                    listLat.add(latLng)
//                    Draw_Map()
                    updateDrawingLine(latLng, map)
                }

                MotionEvent.ACTION_UP -> {
                    Log.d("TAG", "Drawing complete: $listLat")
                    Draw_Map()
                }
            }
            true
        }
    }

    private fun updateDrawingLine(latLng: LatLng, map: GoogleMap) {
        val polylineOptions = PolylineOptions().apply {
            addAll(listLat) // Add all points from listLat
            color(Color.parseColor("#0088ff")) // Light Blue
            width(5f)
        }

        map.addPolyline(polylineOptions)
    }

    fun Draw_Map() {
        val rectOptions = PolygonOptions()
        rectOptions.addAll(listLat)
        rectOptions.strokeColor(Color.parseColor("#0088ff")) // Light Blue
        rectOptions.strokeWidth(5f)
        rectOptions.fillColor(Color.parseColor("#ADD8E6")) // Light Blue

        mMap?.addPolygon(rectOptions)
    }

    private fun addDots(count: Int) {
        mBinding.dotsContainer.removeAllViews()  // Clear any existing dots

        for (i in 0 until count) {
            val dot = ImageView(requireContext())
            dot.setImageResource(R.drawable.dots)  // Set the drawable as the dot image

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 10)  // Space between dots (top & bottom)

            mBinding.dotsContainer.addView(dot, params)
        }
    }
//

    override fun onPause() {
        mBinding.map.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mBinding.map.onDestroy()
//        parentJob.cancel()
        super.onDestroy()

    }


    override fun onLowMemory() {
        mBinding.map.onLowMemory()
        super.onLowMemory()

    }

    private fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.####")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    fun progressVisibility(progressVisible: Boolean) {
        if (progressVisible) {
            mBinding.sUpdateProgressBar.rlPreloader.startAnimation()
            mBinding.sUpdateProgressBar.rlPreloader.visibility = View.VISIBLE
            //   findViewById(R.id.login_scrol).setVisibility(View.GONE);
        } else {
            mBinding.sUpdateProgressBar.rlPreloader.visibility = View.GONE
            //    findViewById(R.id.login_scrol).setVisibility(View.VISIBLE);
        }
    }


    private fun viewToBitmap(view: View): Bitmap? {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    private fun disableMapGestures() {
        mMap.uiSettings.isScrollGesturesEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = false
        mMap.uiSettings.isRotateGesturesEnabled = false
        mMap.uiSettings.isTiltGesturesEnabled = false
    }

    // Method to enable map gestures
    private fun enableMapGestures() {
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true
    }

    private fun addPointToPolyline(latLng: LatLng) {
        polylinePoints.add(latLng)
        if (currentPolyline == null) {
            currentPolyline = mMap.addPolyline(
                PolylineOptions()
                    .addAll(polylinePoints)
                    .color(Color.BLUE)
                    .width(5f)
            )
        } else {
            currentPolyline?.points = polylinePoints
        }
    }

    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//        mBinding.roadMapRadio.isChecked=true
        mBinding.mapModeIcon.setOnClickListener {

            val dialog = BottomSheetDialog(
                requireContext(),
                R.style.BaseBottomSheetDialog1
            )
            val bottomSheetView =
                layoutInflater.inflate(R.layout.map_mode_selection_design, null)

            val closeIcon: AppCompatImageView = bottomSheetView.findViewById(R.id.close_dialog)
            val default: ConstraintLayout = bottomSheetView.findViewById(R.id.default_layout)
            val satellite: ConstraintLayout = bottomSheetView.findViewById(R.id.satellite_layout)
            val terrain: ConstraintLayout = bottomSheetView.findViewById(R.id.Terrain_layout)
            val normal1: AppCompatImageView = bottomSheetView.findViewById(R.id.default1)
            val normal2: AppCompatImageView = bottomSheetView.findViewById(R.id.default2)
            val satellite1: AppCompatImageView = bottomSheetView.findViewById(R.id.satellite1)
            val satellite2: AppCompatImageView = bottomSheetView.findViewById(R.id.satellite2)
            val terrain1: AppCompatImageView = bottomSheetView.findViewById(R.id.terrain1)
            val terrain2: AppCompatImageView = bottomSheetView.findViewById(R.id.terrain2)

            default.setOnClickListener {
                normal2.visibility = View.VISIBLE
                normal1.visibility = View.GONE
                satellite1.visibility = View.VISIBLE
                terrain1.visibility = View.VISIBLE

                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                dialog.dismiss()
            }
            satellite.setOnClickListener {
                satellite2.visibility = View.VISIBLE
                satellite1.visibility = View.GONE
                normal1.visibility = View.VISIBLE
                terrain1.visibility = View.VISIBLE
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                dialog.dismiss()
            }
            terrain.setOnClickListener {

                terrain2.visibility = View.VISIBLE
                terrain1.visibility = View.GONE
                satellite1.visibility = View.VISIBLE
                normal1.visibility = View.VISIBLE
                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                dialog.dismiss()
            }

            //


            closeIcon.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCancelable(true)
            dialog.setContentView(bottomSheetView)
            dialog.show()


        }

        val markerView =
            (requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)!! as LayoutInflater).inflate(
                R.layout.location_marker_design, null
            )

        val itemText =
            markerView.findViewById<AppCompatTextView>(R.id.location_name)
        val cardView =
            markerView.findViewById<ConstraintLayout>(R.id.card)
        val markerIcon =
            markerView.findViewById<AppCompatImageView>(R.id.location_icon)
//        mMap.uiSettings.isZoomGesturesEnabled = true
//        mMap.uiSettings.isScrollGesturesEnabled = true


        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
// Your existing map click listener
        mMap.setOnMapClickListener { latLng ->

            enableMapGestures()

            val currentLatLong = LatLng(latLng.latitude, latLng.longitude)
            placeMarkerOnMap(currentLatLong)

            // Create and show the BottomSheetDialog
            val dialog = BottomSheetDialog(requireContext(), R.style.BaseBottomSheetDialog1)
            val view1 = layoutInflater.inflate(R.layout.streetbottomsheet, null)
            val streetButton = view1.findViewById<AppCompatButton>(R.id.street_button)
            val buildingName = view1.findViewById<AppCompatTextView>(R.id.building_name)
            val latitudeLongitude = view1.findViewById<AppCompatTextView>(R.id.latitude_longitude)
            val addressValue = view1.findViewById<AppCompatTextView>(R.id.address_value)
            val cancel = view1.findViewById<AppCompatImageView>(R.id.cancel)

            val latitude = latLng.latitude
            val longitude = latLng.longitude
            Log.d("TAG", "onMapReady: $longitude-$latitude")

            val latlng1 = roundOffDecimal(latitude)
            val lng1 = roundOffDecimal(longitude)

            latitudeLongitude.text = "$latlng1, $lng1"
            getStreet(latitude, longitude, requireContext()) { streetName ->
                buildingName.text = streetName // Update the TextView with the fetched street name
            }
            addressValue.text =
                getAddress(latitude, longitude) + ", " + buildingName.text.toString() + ", " +
                        getCityName(latitude, longitude) + ", " + getStateName(
                    latitude,
                    longitude
                ) + ", " + getPostalCode(latitude, longitude)

            streetButton.setOnClickListener {
                dialog.dismiss()
                val bundle = Bundle()
                bundle.putDouble("latitude", latitude)
                bundle.putDouble("longitude", longitude)

                findNavController().navigate(R.id.action_google_maps_to_street_view, bundle)
            }

            cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setOnCancelListener {
                dialog.setCancelable(true)
                // Optional: Remove the last marker if you want to clean up on cancel
            }

            dialog.setContentView(view1)
            dialog.show()
//            }
        }

        mBinding.cardView2.setOnClickListener {
            disableMapGestures()
        }
//        mBinding.mapChange.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                apps.srichaitanya.employee.R.id.roadMapRadio -> {
//                    googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
////                    mBinding.roadMapRadio.isChecked=true
//                }
//
//                apps.srichaitanya.employee.R.id.satelliteRadio -> {
//                    googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
////                    mBinding.satelliteRadio.isChecked=true
//                }
//
//            }
//        }

        for (mData in googleMarkerPoints) {
            BitmapDescriptorFactory.fromResource(R.drawable.location)
            itemText.visibility = View.INVISIBLE
            itemText.text = mData.buildingName
            itemText.setBackgroundResource(R.color.white)
            val bitmap1 = Bitmap.createScaledBitmap(
                viewToBitmap(cardView)!!,
                cardView.width,
                cardView.height,
                false
            )

            id = mData.buildingId.toString()
            businessType = mData.businessType
            val smallMaker = BitmapDescriptorFactory.fromBitmap(bitmap1)
            val marker1 = mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        mData.latitude?.toDouble() ?: 0.0,
                        mData.longitude?.toDouble() ?: 0.0
                    )
                ).icon(smallMaker)
            )
            var latlng: LatLng? = null


            mBinding.sourceValue.text = googleMarkerPoints[0].buildingName
            buildingName = googleMarkerPoints[0].buildingName
            latitude = googleMarkerPoints[0].latitude
            longitude = googleMarkerPoints[0].longitude
//            sourceLatLng
            latlng = LatLng(latitude?.toDouble() ?: 0.0, longitude?.toDouble() ?: 0.0)
            marker1?.tag = mData.buildingId

            markers.add(latlng)
            marker1?.let { markerList.add(it) }
//           id = mData.buildingId.toString()
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        googleMarkerPoints[0].latitude?.toDouble() ?: 0.0,
                        googleMarkerPoints[0].longitude?.toDouble() ?: 0.0
                    ), 18F
                )
            )
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12F), 1000, null)

        }
        mMap.setOnMarkerClickListener(CustomMarkerClickListener())
        buildingDetailsViewModel.googleMapsAllBuildings().observe(viewLifecycleOwner) {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        val buildingsData = resource.data


                        mMap.setOnCameraMoveListener {

                            val center = googleMap.cameraPosition.target
                            val cameraPosition = mMap.cameraPosition
                            val zoomLevel = cameraPosition.zoom
                            Log.d("TAG", "zoom: $zoomLevel")
                            textViewArrayList.forEach { name ->
                                name.visibility = if (zoomLevel < 12) View.GONE else View.VISIBLE
                            }

//                            mBinding.button.setOnClickListener {
//                                textViewArrayList.clear()
//                                val target =
//                                    Coordinate(center.latitude, center.longitude, center.toString())
//
//
//                                val density = resources.displayMetrics.density
//                                val screenRadius = calculateScreenRadius(zoomLevel, density)
//
//                                val nearbyCoordinates = buildingsData?.filter {
//                                    val coordinate = Coordinate(
//                                        it.googleLat.toDouble(),
//                                        it.googleLong.toDouble(),
//                                        it.buildingName
//                                    )
//                                    val distance = coordinate.distanceTo(target)
//                                    Log.d("TAG", "Distance with target: $distance")
//                                    Log.d("TAG", "Screen Radius: $screenRadius")
//                                    Log.d("TAG", "Value: ${distance < screenRadius}")
//                                    distance < screenRadius
//
//                                }
//                                nearbyCoordinates?.forEach { coordinate1 ->
//                                    val locationLatLng = LatLng(
//                                        coordinate1.googleLat.toDouble(),
//                                        coordinate1.googleLong.toDouble()
//                                    )
//
//
//
//                                    if (markers.contains(locationLatLng)) {
//                                        Log.i("TAG", "LocationIsAlreadyAdded $markers")
//                                    } else {
//
//                                        Log.d("TAG", "Marker ${coordinate1.googleLat}")
//                                        val markerView =
//                                            (requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)!! as LayoutInflater).inflate(
//                                                apps.srichaitanya.employee.R.layout.location_marker_design_yellow,
//                                                null
//                                            )
//                                        val cardView =
//                                            markerView.findViewById<ConstraintLayout>(R.id.card)
//                                        val itemText: AppCompatTextView = markerView.findViewById(
//                                            R.id.location_name
//                                        )
//
//                                        itemText.text = coordinate1.buildingName
//                                        textViewArrayList.add(itemText)
//                                        val bitmap1 = viewToBitmap(cardView!!)?.let { it1 ->
//                                            Bitmap.createScaledBitmap(
//                                                it1,
//                                                cardView.width,
//                                                cardView.height,
//                                                false
//                                            )
//                                        }
//                                        val smallMaker =
//                                            bitmap1?.let { it1 ->
//                                                BitmapDescriptorFactory.fromBitmap(
//                                                    it1
//                                                )
//                                            }
//
//                                        val newMarker = mMap.addMarker(
//                                            MarkerOptions().position(
//                                                locationLatLng
//                                            ).icon(smallMaker)
//                                        )
////                                        nearCampusRecycler?.adapter =
////                                            NearestCampusLocationListAdapter(
////                                            )
//
//
//                                        newMarker?.tag = coordinate1.buildingId
//
//                                        markers.add(locationLatLng)
//
//                                    }
//                                }
//                            }

                        }


                    }

                    Status.LOADING -> {
                        progressVisibility(false)
                    }

                    Status.ERROR -> {
                        progressVisibility(true)

                    }

                    Status.NETWORK_ERROR -> {
                        progressVisibility(false)
                    }


                    else -> {}
                }

            }
        }
//        }

    }

    override fun onResume() {
        super.onResume()
        mBinding.map.onResume()


//        stringList.clear()

        // Retrieve latitude and longitude safely
        val searchedLat = directionsViewModels.searchedlatitude?.toDoubleOrNull()
        val searchedLng = directionsViewModels.searchedlongitude?.toDoubleOrNull()
        val buildingName = directionsViewModels.buildingName
        val buildingId = directionsViewModels.buildingId

        // Set destination value or default text
        if (buildingName.isNullOrEmpty()) {
            mBinding.destinationValue.text = "Destination Location"
        } else {
            mBinding.destinationValue.text = buildingName
        }

        // Debugging logs for coordinate values
        Log.d("Debug", "Searched Latitude: $searchedLat, Searched Longitude: $searchedLng")
        Log.d("Debug", "Source Latitude: $latitude, Source Longitude: $longitude")

        // Assign source coordinates
        if (latitude != null && longitude != null) {
            sourceLatLng = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
        } else {
            Log.e("Debug", "Source coordinates are null.")
        }

        // Assign destination coordinates
        if (searchedLat != null && searchedLng != null) {
            destinationLatLng = LatLng(searchedLat, searchedLng)
        } else {
            Log.e("Debug", "Destination coordinates are null.")
        }

        // Check if both source and destination are assigned
        if (sourceLatLng != null && destinationLatLng != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    Log.d(
                        "Debug",
                        "Calling drawRoute with Source: $sourceLatLng, Destination: $destinationLatLng"
                    )
                    mMap.uiSettings.isScrollGesturesEnabled = true

                    drawRoute(
                        sourceLatLng!!,
                        destinationLatLng!!,
                        buildingName ?: "",
                        buildingId ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("Debug", "Error in drawRoute: ${e.message}")
                }
            }
        } else {
            Log.e("Debug", "Source or destination coordinates are invalid.")
        }

        if (latitude != null && longitude != null) {
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        latitude?.toDouble() ?: 0.0,
                        longitude?.toDouble() ?: 0.0
                    ), 12F
                )
            )

        } else {
            Log.e("Debug", "Source coordinates are null.")
        }


//        uniqueIdsSelectedBuilding?.clear()
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private suspend fun drawRoute(origin: LatLng, destination: LatLng, name: String, bId: String) {
        try {
            // Use a coroutine to make the Directions API request on a background thread
            val result = withContext(Dispatchers.IO) {
                DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin("${origin.latitude},${origin.longitude}")
                    .destination("${destination.latitude},${destination.longitude}")
                    .avoid(DirectionsApi.RouteRestriction.TOLLS)
                    .await()
            }

            // If route exists, add polyline to the map
            if (result.routes.isNotEmpty()) {
                val decodedPolyline = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)
                val polylineOptions = PolylineOptions()
                    .addAll(decodedPolyline)
                    .color(Color.BLUE)
                    .width(10f)
                    .geodesic(true)
                    .jointType(JointType.ROUND)

                mMap.addPolyline(polylineOptions)


                // Adjust camera to fit the bounds of the route
                val boundsBuilder = LatLngBounds.Builder()
                decodedPolyline.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                mMap.moveCamera(cameraUpdate)


                val markerView =
                    (requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)!! as LayoutInflater).inflate(
                        apps.srichaitanya.mis.R.layout.location_marker_design_yellow,
                        null
                    )
                val cardView =
                    markerView.findViewById<ConstraintLayout>(apps.srichaitanya.mis.R.id.card)
                val itemText: AppCompatTextView = markerView.findViewById(
                    apps.srichaitanya.mis.R.id.location_name
                )

                itemText.visibility = View.INVISIBLE
                itemText.text = name
                textViewArrayList.add(itemText)
                val bitmap1 = viewToBitmap(cardView!!)?.let { it1 ->
                    Bitmap.createScaledBitmap(
                        it1,
                        cardView.width,
                        cardView.height,
                        false
                    )
                }
                val smallMaker =
                    bitmap1?.let { it1 ->
                        BitmapDescriptorFactory.fromBitmap(
                            it1
                        )
                    }

                val newMarker = mMap.addMarker(
                    MarkerOptions().position(
                        destination
                    ).icon(smallMaker)
                )
//                                        nearCampusRecycler?.adapter =
//                                            NearestCampusLocationListAdapter(
//                                            )


                newMarker?.tag = bId

                markers.add(destination)
                newMarker?.let { markerList.add(it) }

                // Optionally place a marker at the destination

//                placeMarkerOnMap(destination)
                val distance = calculateDistance(origin, destination)
                Log.d("TAG", "drawRoute: $distance")
                //  placeDistanceCardOnMap(origin, destination, distance)


                val routeLeg = result.routes[0].legs[0]
                val duration = routeLeg.duration.humanReadable // Example: "20 mins"
                val durationInSeconds = routeLeg.duration.inSeconds
                val hours = durationInSeconds / 3600
                val minutes = (durationInSeconds % 3600) / 60

                val timeValue = view?.findViewById<AppCompatTextView>(R.id.timeText)
                timeValue?.text = duration

//                setBottomSheetVisibility(true)

//                // bottomSheet = 1100
//                bottomSheetBehaviormap2 = BottomSheetBehavior.from(bottomSheetmap2!!)
//                bottomSheetBehaviormap2!!.setPeekHeight(1100,false)
//
//
//
                //    bottomSheetmap2?.visibility=View.VISIBLE
//                val closeButton =view?.findViewById<AppCompatImageView>(R.id.sclose_dialog_)
                var distanceValue = view?.findViewById<AppCompatTextView>(R.id.distanceText)
                var sourceValue = view?.findViewById<AppCompatTextView>(R.id.sourceValueBottom)
                var destinationValue =
                    view?.findViewById<AppCompatTextView>(R.id.destinationValueBottom)
                sourceValue?.text = buildingName


                destinationValue?.text = directionsViewModels.buildingName
                val formattedDistance =
                    String.format("%.1f km", distance) // Round to 1 decimal place and append "km"
                distanceValue?.text = "($formattedDistance)"

                if (destinationValue?.text?.isNotEmpty() == true) {
                    // Directly set the expanded state without manipulating peekHeight or collapsing first
                    bottomSheetBehaviormap2?.state = BottomSheetBehavior.STATE_EXPANDED
                }


            } else {
                // No route found
                Toast.makeText(
                    requireContext(),
                    "No route found. Please check your locations.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e("DirectionsError", "Error fetching directions: ${e.message}")
            e.printStackTrace()
        }
    }

    fun calculateDistance(origin: LatLng, destination: LatLng): Double {
        // Calculate the distance in meters
        val distanceInMeters = SphericalUtil.computeDistanceBetween(origin, destination)

        // If you need the distance in kilometers, simply divide by 1000
        val distanceInKilometers = distanceInMeters / 1000.0

        // Return the distance in kilometers or meters (based on your requirement)
        return distanceInKilometers // For kilometers
        // return distanceInMeters // For meters
    }

//    private fun setBottomSheetVisibility(isVisible: Boolean) {
//        val updatedState = if (isVisible) BottomSheetBehavior.STATE_EXPANDED
//        else BottomSheetBehavior.STATE_COLLAPSED
//        bottomSheetBehaviormap2?.state = updatedState
//        bottomSheetBehaviormap2!!.setPeekHeight(400, false)
//        //  bottomSheetBehavior!!.setPeekHeight(400,false)
//        val desiredHeightInPixels = 200
//        // Set your desired height in pixels
//        bottomSheetBehaviormap2!!.peekHeight = desiredHeightInPixels
//    }

    private fun placeDistanceCardOnMap(origin: LatLng, destination: LatLng, distance: Double) {

        val distanceCard =
            (requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)!! as LayoutInflater).inflate(
                R.layout.distance_card, null
            )

//        map.addView(distanceCard)

        // Calculate the midpoint of the route to place the card
        val midpoint = LatLng(
            (origin.latitude + destination.latitude) / 2,
            (origin.longitude + destination.longitude) / 2
        )
        distanceTextView.text = "Distance: %.2f km".format(distance)


        // Position the card relative to the map
        distanceCard.visibility = View.VISIBLE
        val layoutParams = distanceCard.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = 100 // Adjust to position it dynamically
        layoutParams.topMargin = 200 // Adjust to position it dynamically
        distanceCard.layoutParams = layoutParams
    }

    inner class CustomMarkerClickListener() : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker): Boolean {
            Log.d("TAG", "markerid: ${marker.tag}")
            id = marker.tag.toString()


            val dialog = BottomSheetDialog(
                requireContext(),
                R.style.BaseBottomSheetDialog1
            )
            val view1 = layoutInflater.inflate(
                R.layout.bottom_sheet_maps_building_icons,
                null
            )
            val closeButton =
                view1.findViewById<AppCompatImageView>(R.id.close_dialog)
            val buildingName1 =
                view1.findViewById<AppCompatTextView>(R.id.building_name)
            val campusName =
                view1.findViewById<AppCompatTextView>(R.id.CampusName)
            val sqftBuildingAmountValue =
                view1.findViewById<AppCompatTextView>(R.id.sqftbuildng_amount_value)
            val otherSftValue =
                view1.findViewById<AppCompatTextView>(R.id.othersft_value)
            val totalSftValue =
                view1.findViewById<AppCompatTextView>(R.id.totalsft_value)
            val rentValue =
                view1.findViewById<AppCompatTextView>(R.id.rent_value)
            val gstValue =
                view1.findViewById<AppCompatTextView>(R.id.gst_value)
            val rentWithGstValue =
                view1.findViewById<AppCompatTextView>(R.id.rentwithGst_value)
            val rentWithGstYearValue =
                view1.findViewById<AppCompatTextView>(R.id.rentwithgst_year_value)
            val strengthValue =
                view1.findViewById<AppCompatTextView>(R.id.strength_value)
            val avgSftStudentValue =
                view1.findViewById<AppCompatTextView>(R.id.avgsftstudent_value)
            val avgRentSftValue =
                view1.findViewById<AppCompatTextView>(R.id.avgrentsft_value)
            val advancedValue =
                view1.findViewById<AppCompatTextView>(R.id.advanced_value)
            val estimateStrengthValue =
                view1.findViewById<AppCompatTextView>(R.id.estimateStrength_value)
            val buildingImagesRecycler =
                view1.findViewById<RecyclerView>(R.id.buildingImagesRecycler)
            val noImages =
                view1.findViewById<AppCompatTextView>(R.id.no_images)
            val addressValue =
                view1.findViewById<AppCompatTextView>(R.id.addressValue)
            val stateValue =
                view1.findViewById<AppCompatTextView>(R.id.stateValue)
            val cityValue =
                view1.findViewById<AppCompatTextView>(R.id.city_value)
            val zoneValue =
                view1.findViewById<AppCompatTextView>(R.id.zone_value)
            val subZoneValue =
                view1.findViewById<AppCompatTextView>(R.id.subzone_value)
            val campusValue =
                view1.findViewById<AppCompatTextView>(R.id.campus_value)

            campusStaffViewModel.googleMapsBuildingImages(id ?: "")
                .observe(requireActivity()) {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                progressVisibility(false)
                                val cityResponse = resource.data?.buildingImageUrlList
                                if (cityResponse.isNullOrEmpty()) {
                                    noImages.visibility = View.VISIBLE
                                    buildingImagesRecycler.visibility = View.GONE
                                } else {
                                    noImages.visibility = View.GONE
                                    buildingImagesRecycler.visibility = View.VISIBLE
                                }
                                val layoutManager = LinearLayoutManager(
                                    requireActivity(),
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )

                                buildingImagesRecycler.layoutManager = layoutManager
                                googleMapAdapter = GoogleMapBottomSheetImageRecyclerAdapter(
                                    cityResponse ?: arrayListOf(),
                                    requireActivity()
                                )
                                buildingImagesRecycler.adapter = googleMapAdapter


                            }

                            Status.ERROR -> {
                                progressVisibility(false)
                                noImages.visibility = View.VISIBLE
                                buildingImagesRecycler.visibility = View.GONE
                            }

                            Status.LOADING -> {
                                progressVisibility(true)
                            }

                            Status.NETWORK_ERROR -> {
                                progressVisibility(false)
                            }

                            else -> {}
                        }


                    }
                }


            val googleMapRequest = GoogleMapRequest(businessType ?: "", "BUILDING", id ?: "")
            campusStaffViewModel.googleMaps(googleMapRequest)
                .observe(viewLifecycleOwner) {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                progressVisibility(false)

                                val totalResponse = resource.data
                                Log.d("TAG", "totalResponse:$totalResponse ")

                                buildingName1.text =
                                    totalResponse?.get(0)?.leaseMapDetails?.buildingName
                                campusName.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.campusName
                                sqftBuildingAmountValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.sqFtBui.toString())
                                otherSftValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.otherSqft.toString())
                                totalSftValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.totalSqft.toString())
                                rentValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.rentBui.toString()
                                        .let {
                                            " ₹ " + EmployeeSharedPreference.getIndianCurrencyFormat(
                                                it
                                            )
                                        }

                                gstValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.gst.toString()
                                        .let {
                                            " ₹ " + EmployeeSharedPreference.getIndianCurrencyFormat(
                                                it
                                            )
                                        }

                                rentWithGstValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.rentWithGst.toString()
                                        .let {
                                            " ₹ " + EmployeeSharedPreference.getIndianCurrencyFormat(
                                                it
                                            )
                                        }

                                rentWithGstYearValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.rentWithGstYr.toString()
                                        .let {
                                            " ₹ " + EmployeeSharedPreference.getIndianCurrencyFormat(
                                                it
                                            )
                                        }

                                strengthValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.strn.toString())
                                avgSftStudentValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.avgSqFtStu.toString())
                                avgRentSftValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.avgRentSqFt.toString())
                                advancedValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.advance.toString()
                                        .let {
                                            " ₹ " + EmployeeSharedPreference.getIndianCurrencyFormat(
                                                it
                                            )
                                        }

                                estimateStrengthValue.text =
                                    getIndianCurrencyFormat(totalResponse?.get(0)?.buildingLeaseDetails?.estimatedStrn.toString())
                                if (totalResponse?.get(0)?.leaseMapDetails?.address.isNullOrEmpty()) {
                                    addressValue.text = "-"
                                } else {
                                    addressValue.text =
                                        totalResponse?.get(0)?.leaseMapDetails?.address.toString()
                                }
                                stateValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.stateName
                                cityValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.cityName
                                zoneValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.zone
                                subZoneValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.subZone
                                campusValue.text =
                                    totalResponse?.get(0)?.buildingLeaseDetails?.campusName


                            }

                            Status.ERROR -> {
                                progressVisibility(false)

                            }

                            Status.LOADING -> {
                                progressVisibility(true)
                            }

                            Status.NETWORK_ERROR -> {
                                progressVisibility(false)

                            }

                            else -> {}
                        }
                    }
                }



            closeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setCancelable(false)
            dialog.setContentView(view1)
            dialog.show()
            true
            marker.showInfoWindow()
            return true
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        Log.d("TAG", "placeMarkerOnMap: $currentLatLong ")
        MarkerOptions().position(currentLatLong)

        markerName = mMap.addMarker(
            MarkerOptions().position(currentLatLong).title("Title").icon(
                bitmapFromVector(
                    requireContext(),
                    R.drawable.map_location_icon
                )
            )
        )!!


    }

    private fun destinationMarker(currentLatLong: LatLng) {
        val customMarkerOptions = MarkerOptions()
            .position(currentLatLong)
            .title("Destination")
            .icon(BitmapDescriptorFactory.fromResource(R.layout.location_marker_design))

        mMap.addMarker(customMarkerOptions)


    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        //drawable generator
        val vectorDrawable: Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap: Bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        //pass bitmap in canvas constructor
        val canvas = Canvas(bitmap)
        //pass canvas in drawable
        vectorDrawable.draw(canvas)
        //return BitmapDescriptorFactory
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    //    private fun getStreet(lat: Double, long: Double): String {
//        var street = ""
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        val address: MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
//        address?.let {
//            if (address.isNotEmpty()) {
//                street = address[0].subLocality ?: "---"
//            }
//        }
//        return street
//
//    }
    fun getStreet(lat: Double, long: Double, context: Context, onResult: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(context, Locale.getDefault())
            var street = "---" // Default value
            try {
                val addressList = geocoder.getFromLocation(lat, long, 1)
                if (!addressList.isNullOrEmpty()) {
                    street = addressList[0].subLocality ?: "---"
                }
            } catch (e: IOException) {
                Log.e("GeocoderError", "Failed to get address: ${e.message}")
            } catch (e: IllegalArgumentException) {
                Log.e("GeocoderError", "Invalid latitude or longitude: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                onResult(street) // Return the result to the UI thread
            }
        }
    }


    private fun getPostalCode(lat: Double, long: Double): String {

        var postalcode = ""
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address: MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
        address?.let {
            if (address.isNotEmpty()) {
                postalcode = address[0].postalCode ?: "---"
            }
        }
        return postalcode

    }

    private fun getStateName(lat: Double, long: Double): String {

        var stateName = "---"
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address: MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
        address?.let {
            if (address.isNotEmpty()) {
                stateName = address[0].adminArea ?: "---"
            }
        }
        return stateName

    }

    fun getIndianCurrencyFormat(amount: String): String {
        var amountStr = amount
        if (amountStr.indexOf(".") > -1) {
            amountStr = amountStr.substring(0, amountStr.indexOf("."))
        }
        val stringBuilder = StringBuilder()
        val amountArray = amountStr.toCharArray()
        var a = 0
        var b = 0
        for (i in amountArray.indices.reversed()) {
            if (a < 3) {
                stringBuilder.append(amountArray[i])
                a++
            } else if (b < 2) {
                if (b == 0) {
                    stringBuilder.append(",")
                    stringBuilder.append(amountArray[i])
                    b++
                } else {
                    stringBuilder.append(amountArray[i])
                    b = 0
                }
            }
        }
        return stringBuilder.reverse().toString()
    }

    private fun getAddress(lat: Double, long: Double): String {
        var address = "---"
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList: MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
        addressList?.let {
            if (addressList.isNotEmpty()) {
                address = addressList[0].featureName ?: "---"
            }
        }
        return address
    }

    private fun getCityName(lat: Double, long: Double): String {

        var cityName = "----"
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address: MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
        address?.let {
            if (address.isNotEmpty()) {
                cityName = address[0].locality ?: "---"
            }
        }
        return cityName

    }

    private fun calculateScreenRadius(zoomLevel: Float, density: Float): Double {
        // The following formula calculates the screen radius in meters based on zoom level and screen density.
        val zoomToRadiusFactor =
            156543.03392 // A constant factor for zoom level to meters conversion
        val radius = zoomToRadiusFactor / 2.0.pow(zoomLevel.toDouble()) / density
        return radius
    }

    data class Coordinate(val latitude: Double, val longitude: Double, val s: String) {
        // You can add a method to calculate the distance to another coordinate using the Haversine formula.
        fun distanceTo(other: Coordinate): Double {
            val earthRadius = 6371.0 // Radius of the Earth in kilometers
            val lat1 = Math.toRadians(latitude)
            val lon1 = Math.toRadians(longitude)
            val lat2 = Math.toRadians(other.latitude)
            val lon2 = Math.toRadians(other.longitude)

            val dLat = lat2 - lat1
            val dLon = lon2 - lon1

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(lat1) * cos(lat2) *
                    sin(dLon / 2) * sin(dLon / 2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return earthRadius * c
        }
    }

    override fun onShapeCompleted(shapeType: ShapeType, shapeId: String) {
    }

    override fun onShapeUpdated(shapeType: ShapeType, shapeId: String) {
    }

    override fun onAllShapeRemove() {

    }

    override fun onShapeRemoveAfter(deleted: Boolean) {
    }

    override fun onShapeRemoveBefore(shapeType: ShapeType, shapeIndex: Int, shapeCount: Int) {
    }

    override fun onShapeRemoveModeEnabled(removeModeEnable: Boolean) {
    }
}