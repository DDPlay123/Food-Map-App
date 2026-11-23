import GoogleMaps
import GoogleMapsUtils
import Shared
import UIKit

/// 交給 Kotlin 的地圖工廠，實作 Shared 的 `NativeMapFactory`。
final class GoogleMapFactory: NSObject, NativeMapFactory {
    func create(initialCenter: GeoPoint, initialZoom: Float, maxZoom: Float) -> any NativeMapView {
        GoogleMapView(center: initialCenter, zoom: initialZoom, maxZoom: maxZoom)
    }
}

/// 群集用的餐廳項目，對應 Android 端的 RestaurantClusterItem。
final class RestaurantItem: NSObject, GMUClusterItem {
    let marker: RestaurantMarker
    let isSelected: Bool
    let position: CLLocationCoordinate2D

    init(marker: RestaurantMarker, isSelected: Bool) {
        self.marker = marker
        self.isSelected = isSelected
        self.position = marker.position.coordinate
        super.init()
    }
}

/// 以 GMSMapView + GMUClusterManager 實作 Shared 的 `NativeMapView`。
/// 互動規則與 Android（maps-compose）版本相同。
final class GoogleMapView: NSObject, NativeMapView {
    let view: UIView

    private let mapView: GMSMapView
    private let clusterManager: GMUClusterManager
    private var routeLine: GMSPolyline?
    // Kotlin 端建立的 listener 只由這裡持有，必須是強參考
    private var listener: (any NativeMapListener)?

    init(center: GeoPoint, zoom: Float, maxZoom: Float) {
        let options = GMSMapViewOptions()
        options.camera = GMSCameraPosition(latitude: center.latitude, longitude: center.longitude, zoom: zoom)
        let mapView = GMSMapView(options: options)
        mapView.setMinZoom(kGMSMinZoomLevel, maxZoom: maxZoom)

        let renderer = GMUDefaultClusterRenderer(
            mapView: mapView,
            clusterIconGenerator: GMUDefaultClusterIconGenerator()
        )
        let clusterManager = GMUClusterManager(
            map: mapView,
            algorithm: GMUNonHierarchicalDistanceBasedAlgorithm(),
            renderer: renderer
        )

        self.mapView = mapView
        self.clusterManager = clusterManager
        self.view = mapView
        super.init()

        renderer.delegate = self
        clusterManager.setDelegate(self, mapDelegate: nil)
    }

    func setListener(listener: (any NativeMapListener)?) {
        self.listener = listener
    }

    func setMarkers(markers: [RestaurantMarker], selectedMarkerId: String?) {
        let items: [GMUClusterItem] = markers.map {
            RestaurantItem(marker: $0, isSelected: $0.id == selectedMarkerId)
        }
        clusterManager.clearItems()
        clusterManager.add(items)
        clusterManager.cluster()
    }

    func setRoute(points: [GeoPoint]) {
        routeLine?.map = nil
        routeLine = nil
        guard points.count >= 2 else { return }

        let path = GMSMutablePath()
        points.forEach { path.add($0.coordinate) }
        let line = GMSPolyline(path: path)
        line.strokeWidth = 5
        line.strokeColor = .brand
        line.map = mapView
        routeLine = line
    }

    func focus(target: GeoPoint, zoom: Float) {
        if zoom > 0 {
            mapView.animate(to: GMSCameraPosition(target: target.coordinate, zoom: zoom))
        } else {
            mapView.animate(toLocation: target.coordinate)
        }
    }

    func fit(points: [GeoPoint], paddingPoints: Double) {
        guard !points.isEmpty else { return }
        let bounds = points.reduce(GMSCoordinateBounds()) { $0.includingCoordinate($1.coordinate) }
        mapView.animate(with: GMSCameraUpdate.fit(bounds, withPadding: CGFloat(paddingPoints)))
    }

    func setStyle(styleJson: String?) {
        mapView.mapStyle = styleJson.flatMap { try? GMSMapStyle(jsonString: $0) }
    }
}

// MARK: - 群集點擊

extension GoogleMapView: GMUClusterManagerDelegate {
    func clusterManager(_ clusterManager: GMUClusterManager, didTap cluster: GMUCluster) -> Bool {
        let items = cluster.items.compactMap { $0 as? RestaurantItem }
        if mapView.camera.zoom >= mapView.maxZoom - 0.5 {
            // 已達最大縮放仍未分開（例如美食街），交給共用程式碼列出清單
            listener?.onClusterAtMaxZoom(markerIds: items.map { $0.marker.id })
        } else {
            let bounds = items.reduce(GMSCoordinateBounds()) { $0.includingCoordinate($1.position) }
            mapView.animate(with: GMSCameraUpdate.fit(bounds, withPadding: 80))
        }
        return true
    }

    func clusterManager(_ clusterManager: GMUClusterManager, didTap clusterItem: GMUClusterItem) -> Bool {
        guard let item = clusterItem as? RestaurantItem else { return false }
        listener?.onMarkerClick(markerId: item.marker.id)
        return true
    }
}

// MARK: - 自訂 Marker 外觀

extension GoogleMapView: GMUClusterRendererDelegate {
    func renderer(_ renderer: GMUClusterRenderer, willRenderMarker marker: GMSMarker) {
        guard let item = marker.userData as? RestaurantItem else { return }
        marker.iconView = RestaurantPinView(marker: item.marker, selected: item.isSelected)
        marker.groundAnchor = CGPoint(x: 0.5, y: 1.0)
        marker.zIndex = item.isSelected ? 1 : 0
        marker.tracksViewChanges = false
    }
}

// MARK: - 共用小工具

extension GeoPoint {
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}

extension UIColor {
    /// 品牌主色 #F52C56，對應 App 的 colors.xml `primary`。
    static let brand = UIColor(red: 245 / 255, green: 44 / 255, blue: 86 / 255, alpha: 1)
}
