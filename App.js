import React, { useEffect, useRef, useState } from 'react';
import {
  View,
  Text,
  PermissionsAndroid,
  Platform,
  TouchableOpacity,
  Alert,
} from 'react-native';
import MapView, { Marker } from 'react-native-maps';
import Geolocation from 'react-native-geolocation-service';

const App = () => {
  const [location, setLocation] = useState(null);
  const [cameraPosition, setCameraPosition] = useState(null);
  const mapRef = useRef(null);

  useEffect(() => {
    requestLocationPermission();
  }, []);

  const requestLocationPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          {
            title: 'Location Permission',
            message: 'App needs access to your location',
            buttonPositive: 'OK',
          },
        );
        if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
          Alert.alert('Permission Denied', 'Location permission is required');
        }
      } catch (err) {
        console.warn(err);
      }
    }
  };

  const getLocation = () => {
    Geolocation.getCurrentPosition(
      position => {
        const { latitude, longitude } = position.coords;
        const currentLocation = {
          latitude,
          longitude,
          latitudeDelta: 0.01,
          longitudeDelta: 0.01,
        };
        setLocation(currentLocation);

        // Move the camera to the new location
        if (mapRef.current) {
          mapRef.current.animateToRegion(currentLocation, 1000);
        }
      },
      error => {
        console.warn(error);
        Alert.alert('Error', 'Failed to get current location');
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 },
    );
  };

  const handleCameraMove = region => {
    setCameraPosition(region);
    console.log('Camera moving:', region);
  };

  const handleCameraMoveEnd = region => {
    setCameraPosition(region);
    console.log('Camera stopped moving:', region);
  };

  return (
    <View style={{ flex: 1 }}>
      <MapView
        ref={mapRef}
        style={{ flex: 1 }}
        region={
          location || {
            latitude: 28.693602091083623,
            longitude: 77.21464383448563,
            latitudeDelta: 0.01,
            longitudeDelta: 0.01,
          }
        }
        showsUserLocation={true}
        onRegionChange={handleCameraMove}
        onRegionChangeComplete={handleCameraMoveEnd}
      >
        {location && <Marker coordinate={location} />}
      </MapView>

      <TouchableOpacity
        style={{
          position: 'absolute',
          bottom: 30,
          left: 20,
          right: 20,
          height: 50,
          backgroundColor: 'green',
          justifyContent: 'center',
          alignItems: 'center',
          borderRadius: 10,
        }}
        onPress={getLocation}
      >
        <Text style={{ color: 'white', fontWeight: 'bold' }}>
          Get Current Location
        </Text>
      </TouchableOpacity>

      {cameraPosition && (
        <View
          style={{
            position: 'absolute',
            top: 50,
            left: 10,
            right: 10,
            backgroundColor: 'rgba(0,0,0,0.5)',
            padding: 10,
            borderRadius: 10,
          }}
        >
          <Text style={{ color: '#fff' }}>
            Camera: {cameraPosition.latitude.toFixed(5)},{' '}
            {cameraPosition.longitude.toFixed(5)}
          </Text>
        </View>
      )}
    </View>
  );
};

export default App;
