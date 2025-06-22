import SwiftUI

@objc public class KtorinUI: NSObject {
    @objc public static func view() -> some View {
        return KtorinView()
    }
}

struct KtorinView: View {
    var body: some View {
        Text("🕵️ Network Monitor View")
            .font(.title)
            .padding()
    }
}
