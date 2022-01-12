package cf.hydos.renderer.scene;

import org.joml.Vector3f;

import cf.hydos.renderer.animatedModel.AnimatedModel;

/**
 * Represents all the stuff in the com.thinmatrix.animationrenderer.scene (just the camera, light, and model
 * really).
 * 
 * @author Karl
 *
 */
public class Scene {

	private final ICamera camera;

	private final AnimatedModel animatedModel;

	private Vector3f lightDirection = new Vector3f(0, -1, 0);

	public Scene(AnimatedModel model, ICamera cam) {
		this.animatedModel = model;
		this.camera = cam;
	}

	/**
	 * @return The com.thinmatrix.animationrenderer.scene's camera.
	 */
	public ICamera getCamera() {
		return camera;
	}

	public AnimatedModel getAnimatedModel() {
		return animatedModel;
	}

	/**
	 * @return The direction of the light as a vector.
	 */
	public Vector3f getLightDirection() {
		return lightDirection;
	}

	public void setLightDirection(Vector3f lightDir) {
		this.lightDirection.set(lightDir);
	}

}