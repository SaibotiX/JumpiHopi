package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

public class TestCollisionListener extends SimpleApplication implements PhysicsCollisionListener, ActionListener {
    private BulletAppState bulletAppState;
    private Node shootables;
    private Geometry mark;

    public static void main(String[] args) {
        TestCollisionListener app = new TestCollisionListener();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);

        // Create the physics test world
        createPhysicsTestWorld();
        
        // Setup shooting
        setupShooting();
        
        // Add collision listener
        getPhysicsSpace().addCollisionListener(this);
    }

    private void createPhysicsTestWorld() {
        // Create a box as a target
        Box box = new Box(1f, 1f, 1f);
        Geometry boxGeometry = new Geometry("Box", box);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Blue);
        boxGeometry.setMaterial(boxMat);
        boxGeometry.setLocalTranslation(new Vector3f(0, 2, -10));
        
        // Add physics control to the box
        RigidBodyControl boxPhysics = new RigidBodyControl(1.0f);
        boxGeometry.addControl(boxPhysics);
        
        // Add to scene and physics space
        rootNode.attachChild(boxGeometry);
        getPhysicsSpace().add(boxPhysics);

        // Create ground
        Box floor = new Box(10f, 0.1f, 10f);
        Geometry floorGeometry = new Geometry("Floor", floor);
        Material floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setColor("Color", ColorRGBA.Gray);
        floorGeometry.setMaterial(floorMat);
        floorGeometry.setLocalTranslation(0, -4, -10);
        
        // Add physics control to the floor
        RigidBodyControl floorPhysics = new RigidBodyControl(0.0f); // 0 mass = static
        floorGeometry.addControl(floorPhysics);
        
        // Add to scene and physics space
        rootNode.attachChild(floorGeometry);
        getPhysicsSpace().add(floorPhysics);
    }

    private void setupShooting() {
        // Configure cam to look at scene
        cam.setLocation(new Vector3f(0, 0, 10));
        cam.lookAt(new Vector3f(0, 0, -10), Vector3f.UNIT_Y);
        
        // Add input mapping for shooting
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "shoot");
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("shoot") && !isPressed) {
            // Create bullet
            Sphere bullet = new Sphere(32, 32, 0.4f, true, false);
            bullet.setTextureMode(TextureMode.Projected);
            Geometry bulletGeometry = new Geometry("bullet", bullet);
            Material bulletMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            bulletMat.setColor("Color", ColorRGBA.Red);
            bulletGeometry.setMaterial(bulletMat);
            
            // Position the bullet
            bulletGeometry.setLocalTranslation(cam.getLocation());
            
            // Add physics control
            RigidBodyControl bulletPhysics = new RigidBodyControl(1f);
            bulletGeometry.addControl(bulletPhysics);
            
            // Add to scene and physics space
            rootNode.attachChild(bulletGeometry);
            getPhysicsSpace().add(bulletPhysics);
            
            // Give it speed
            bulletPhysics.setLinearVelocity(cam.getDirection().mult(25));
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if ("Box".equals(event.getNodeA().getName()) || "Box".equals(event.getNodeB().getName())) {
            if ("bullet".equals(event.getNodeA().getName()) || "bullet".equals(event.getNodeB().getName())) {
                fpsText.setText("You hit the box!");
            }
        }
    }
}
