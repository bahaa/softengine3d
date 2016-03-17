package info.bahaa.softengine3d;

import info.bahaa.softengine3d.engine.Camera;
import info.bahaa.softengine3d.engine.Device;
import info.bahaa.softengine3d.engine.Mesh;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.vecmath.Vector3d;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bahaazaid on 3/15/2016.
 */
public class SoftEngineApplication extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private WritableImage writableImage;
    private AnimationTimer animationTimer;
    private GraphicsContext gc;

    private Device device;

    private final double[] frameRates = new double[128];
    private int nextFrameRateIndex = 0;
    private long lastFrameTimestamp = 0;

    private List<Mesh> meshes = new ArrayList<>();
    private Camera camera = new Camera();
    private double rotation = 0.0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("SoftEngine 3D");
        Group root = new Group();

        this.device = new Device(WIDTH, HEIGHT);
        this.writableImage = new WritableImage(WIDTH, HEIGHT);
        this.initEngine();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        final Label frameRateLabel = new Label("Frame Rate: 00 fps");
        frameRateLabel.setTextFill(Color.GRAY);
        root.getChildren().add(frameRateLabel);

        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateFrameRate(now);
                animate(now);
                frameRateLabel.setText(String.format("Frame Rate: %.3f fps", averageFrameRate()));
            }
        };
        this.animationTimer.start();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void initEngine() {
        this.meshes.add(
                new Mesh("Cube")
                        .addVertex(-1, 1, 1)
                        .addVertex(1, 1, 1)
                        .addVertex(-1, -1, 1)
                        .addVertex(1, -1, 1)
                        .addVertex(-1, 1, -1)
                        .addVertex(1, 1, -1)
                        .addVertex(1, -1, -1)
                        .addVertex(-1, -1, -1)

                        .addFace(0, 1, 2)
                        .addFace(1, 2, 3)
                        .addFace(1, 3, 6)
                        .addFace(1, 5, 6)
                        .addFace(0, 1, 4)
                        .addFace(1, 4, 5)
                        .addFace(2, 3, 7)
                        .addFace(3, 6, 7)
                        .addFace(0, 2, 7)
                        .addFace(0, 4, 7)
                        .addFace(4, 5, 6)
                        .addFace(4, 6, 7)
        );

        camera.setPosition(new Vector3d(0, 0, 10));
    }

    private void animate(long now) {
        PixelWriter pixelWriter = this.writableImage.getPixelWriter();

        rotation += 0.01;
        for (Mesh mesh : this.meshes) {
            mesh.transform.rotY(rotation);
        }

        device.clear();
        device.render(camera, this.meshes);

        WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
        pixelWriter.setPixels(0, 0, WIDTH, HEIGHT, pixelFormat, device.getBuffer(), 0, device.getWidth());

        gc.drawImage(this.writableImage, 0.0, 0.0);
    }

    private void updateFrameRate(long now) {
        if (this.lastFrameTimestamp > 0) {
            this.frameRates[this.nextFrameRateIndex % this.frameRates.length] = 1000_000_000.0 / (now - lastFrameTimestamp);
            this.nextFrameRateIndex++;
        }

        this.lastFrameTimestamp = now;
    }

    private double averageFrameRate() {
        if (this.nextFrameRateIndex < this.frameRates.length) {
            return 0.0;
        }

        double sum = 0.0;
        for (double fps : this.frameRates) {
            sum += fps;
        }

        return sum / this.frameRates.length;
    }
}
