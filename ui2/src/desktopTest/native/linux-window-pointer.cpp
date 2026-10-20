// Native pointer input for the same JVM tests on an isolated KWin Wayland desktop.
// Build: c++ linux-window-pointer.cpp -o native-pointer $(pkg-config --cflags --libs Qt6Core wayland-client) -I/usr/include/KWayland -lKWaylandClient
// Run desktopTest with SALT_TEST_NATIVE_POINTER pointing to this executable.
// X11 CI uses java.awt.Robot and does not require this helper.
#include <KWayland/Client/fakeinput.h>
#include <KWayland/Client/registry.h>
#include <QCoreApplication>
#include <QPointF>
#include <wayland-client.h>
#include <iostream>
#include <sstream>

int main(int argc, char **argv) {
    QCoreApplication app(argc, argv);
    auto display = wl_display_connect(nullptr);
    if (!display) return 1;
    KWayland::Client::Registry registry;
    KWayland::Client::FakeInput *input = nullptr;
    QObject::connect(&registry, &KWayland::Client::Registry::fakeInputAnnounced,
        [&](quint32 name, quint32 version) { input = registry.createFakeInput(name, version); });
    registry.create(display);
    registry.setup();
    wl_display_roundtrip(display);
    if (!input) return 2;
    input->authenticate("SaltUI window gesture tests", "Isolated native pointer input");
    wl_display_roundtrip(display);
    std::cout << "ready" << std::endl;
    std::string line;
    while (std::getline(std::cin, line)) {
        std::istringstream args(line);
        std::string command;
        args >> command;
        if (command == "move") {
            double x, y;
            if (!(args >> x >> y)) return 3;
            input->requestPointerMoveAbsolute(QPointF(x, y));
        } else if (command == "press") {
            input->requestPointerButtonPress(Qt::LeftButton);
        } else if (command == "release") {
            input->requestPointerButtonRelease(Qt::LeftButton);
        } else {
            return 3;
        }
        if (wl_display_roundtrip(display) < 0) return 4;
        std::cout << "ok" << std::endl;
    }
    input->requestPointerButtonRelease(Qt::LeftButton);
    wl_display_roundtrip(display);
}
