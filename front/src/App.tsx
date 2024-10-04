import {useCallback, useState} from 'react';
import {
  GoogleMap,
  Polygon,
  Marker,
  InfoWindow,
  useJsApiLoader, DrawingManager, Libraries
} from '@react-google-maps/api';

const containerStyle = {
  width: '100%',
  height: '80vh',
};

const center = {
  lat: 37.5665,
  lng: 126.9780,
};
const libraries: Libraries = ['drawing'];
function App() {
  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '',
    libraries: libraries
  })
  const [path, setPath] = useState<google.maps.LatLngLiteral[]>([]);
  const [completedPolygons, setCompletedPolygons] = useState<google.maps.LatLngLiteral[][]>([]);
  const [infoWindowPosition, setInfoWindowPosition] = useState<google.maps.LatLngLiteral | null>(null);
  const [map, setMap] = useState<google.maps.Map|null>(null)

  const onLoad = useCallback(function callback(map: google.maps.Map) {
    const bounds = new window.google.maps.LatLngBounds(center);
    map.fitBounds(bounds);

    setMap(map)
  }, [])

  const onUnmount = useCallback(function callback() {
    setMap(null)
  }, [])
  const onMapClick = (event: google.maps.MapMouseEvent) => {
    if (event.latLng) {
      const newPoint = {
        lat: event.latLng.lat(),
        lng: event.latLng.lng(),
      };
      setPath([...path, newPoint]);
    }
  };

  const completePolygon = () => {
    if (path.length > 2) {
      setCompletedPolygons([...completedPolygons, path]);
      setPath([]);
    } else {
      alert('폴리곤을 완성하려면 최소 세 개의 점이 필요합니다.');
    }
  };

  const clearPolygons = () => {
    setCompletedPolygons([]);
    setPath([]);
  };
  if (!isLoaded) return <div>Loading...</div>;
  return (
      <>
        <GoogleMap
            onClick={onMapClick}
            mapContainerStyle={containerStyle}
            center={center}
            zoom={10}
            onLoad={onLoad}
            onUnmount={onUnmount}
        >
          {path.map((position, index) => (
              <Marker key={`marker-${index}`} position={position} />
          ))}

          <DrawingManager
              onLoad={(drawingManager) => {
                drawingManager.setMap(map)
              }}
              options={{
                drawingMode: window.google.maps.drawing.OverlayType.MARKER,
                drawingControl: true,
                drawingControlOptions: {
                  position: window.google.maps.ControlPosition.TOP_CENTER,
                  drawingModes: [
                    window.google.maps.drawing.OverlayType.MARKER,
                    window.google.maps.drawing.OverlayType.CIRCLE,
                    window.google.maps.drawing.OverlayType.POLYGON,
                    window.google.maps.drawing.OverlayType.POLYLINE,
                    window.google.maps.drawing.OverlayType.RECTANGLE,
                  ],
                },
                markerOptions: {
                  icon: 'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png',
                },
                circleOptions: {
                  fillColor: '#ffff00',
                  fillOpacity: 1,
                  strokeWeight: 5,
                  clickable: false,
                  editable: true,
                  zIndex: 1,
                },
              }}
              onMarkerComplete={(marker) => {
                console.log('Marker complete:', marker);
              }}
              onCircleComplete={(circle) => {
                console.log('Circle complete:', circle);
              }}
              onPolygonComplete={(polygon) => {
                console.log('Polygon complete:', polygon);
              }}
              onPolylineComplete={(polyline) => {
                console.log('Polyline complete:', polyline);
              }}
              onRectangleComplete={(rectangle) => {
                console.log('Rectangle complete:', rectangle);
              }}
          />

          {completedPolygons.map((polygonPath, index) => (
              <Polygon
                  key={`polygon-${index}`}
                  paths={polygonPath}
                  options={{
                    fillColor: '#2196F3',
                    fillOpacity: 0.4,
                    strokeColor: '#2196F3',
                    strokeOpacity: 0.8,
                    strokeWeight: 2,
                    clickable: true,
                    draggable: false,
                    editable: false,
                    geodesic: false,
                    zIndex: 1,
                  }}
                  onClick={(event) => {
                    if (event.latLng) {
                      setInfoWindowPosition({
                        lat: event.latLng.lat(),
                        lng: event.latLng.lng(),
                      });
                    }
                  }}
              />
          ))}

          {infoWindowPosition && (
              <InfoWindow
                  position={infoWindowPosition}
                  onCloseClick={() => setInfoWindowPosition(null)}
              >
                <div>폴리곤 정보</div>
              </InfoWindow>
          )}
        </GoogleMap>
        <div style={{ position: 'absolute', top: 10, left: 10 }}>
          <button onClick={completePolygon}>폴리곤 완성</button>
          <button onClick={clearPolygons}>초기화</button>
        </div>
      </>
  );
}

export default App;