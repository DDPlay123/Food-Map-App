import Shared
import UIKit

/// 地圖上的餐廳 Marker，外觀規格與 Android 的 `RestaurantPin`（Compose）相同：
/// 圓角標籤顯示店名與評分，選取時以品牌色填滿，下方有指向座標的小三角形。
final class RestaurantPinView: UIView {
    private static let tailHeight: CGFloat = 6

    init(marker: RestaurantMarker, selected: Bool) {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .semibold)
        label.textColor = selected ? .white : .label
        if let rating = marker.rating?.doubleValue {
            label.text = "\(marker.name)  ★\(rating)"
        } else {
            label.text = marker.name
        }
        label.sizeToFit()

        let bubbleSize = CGSize(width: label.bounds.width + 20, height: label.bounds.height + 10)
        super.init(frame: CGRect(x: 0, y: 0, width: bubbleSize.width, height: bubbleSize.height + Self.tailHeight))

        let bubble = UIView(frame: CGRect(origin: .zero, size: bubbleSize))
        bubble.backgroundColor = selected ? .brand : .systemBackground
        bubble.layer.cornerRadius = 12
        bubble.layer.borderWidth = 1
        bubble.layer.borderColor = UIColor.brand.cgColor
        label.center = CGPoint(x: bubbleSize.width / 2, y: bubbleSize.height / 2)
        bubble.addSubview(label)
        addSubview(bubble)

        let tailPath = UIBezierPath()
        tailPath.move(to: CGPoint(x: bubbleSize.width / 2 - 6, y: bubbleSize.height - 0.5))
        tailPath.addLine(to: CGPoint(x: bubbleSize.width / 2 + 6, y: bubbleSize.height - 0.5))
        tailPath.addLine(to: CGPoint(x: bubbleSize.width / 2, y: bubbleSize.height + Self.tailHeight))
        tailPath.close()
        let tail = CAShapeLayer()
        tail.path = tailPath.cgPath
        tail.fillColor = UIColor.brand.cgColor
        layer.addSublayer(tail)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }
}
