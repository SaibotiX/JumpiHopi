package mygame;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.math.ColorRGBA;

public class Player {
    private GameMain app;
    private Node playerNode;
    private Vector3f walkDirection = new Vector3f();
    private boolean forward, backward, left, right;
    private float moveSpeed = 10f;
    private int health = 100;
    private float karma = 0f;
    
    public Player(GameMain app) {
        this.app = app;
        initPlayer();
    }
    
    private void initPlayer() {
        playerNode = new Node("Player");
        
        // Create player model (temporary box)
        Box box = new Box(0.5f, 1f, 0.5f);
        Geometry playerGeom = new Geometry("PlayerGeom", box);
        Material mat = new Material(app.getAssetManager(), 
            "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        playerGeom.setMaterial(mat);
        playerNode.attachChild(playerGeom);
        
        // Set initial position
        playerNode.setLocalTranslation(0, 0, 0);
        
        app.getRootNode().attachChild(playerNode);
    }
    
    public void handleInput(String binding, boolean isPressed) {
        switch (binding) {
            case "Forward": forward = isPressed; break;
            case "Backward": backward = isPressed; break;
            case "Left": left = isPressed; break;
            case "Right": right = isPressed; break;
            case "Jump": 
                // Jump will be implemented later with physics
                break;
            case "Attack":
                if (isPressed) attack();
                break;
        }
    }
    
    public void update(float tpf) {
        // Basic movement without physics
        Vector3f camDir = app.getCamera().getDirection().mult(moveSpeed).clone();
        Vector3f camLeft = app.getCamera().getLeft().mult(moveSpeed).clone();
        camDir.y = 0; // Keep movement in horizontal plane
        camLeft.y = 0;
        
        Vector3f mov = new Vector3f(0, 0, 0);
        if (forward) mov.addLocal(camDir);
        if (backward) mov.addLocal(camDir.negate());
        if (left) mov.addLocal(camLeft);
        if (right) mov.addLocal(camLeft.negate());
        
        mov.multLocal(tpf);
        playerNode.move(mov);
    }
    
    private void attack() {
        // Will implement later
    }
    
    public Vector3f getPosition() {
        return playerNode.getLocalTranslation();
    }
    
    public void adjustKarma(float amount) {
        karma += amount;
        System.out.println("Karma adjusted to: " + karma);
    }
    
    public void damage(int amount) {
        health -= amount;
        if (health <= 0) {
            die();
        }
    }
    
    private void die() {
        health = 100;
        playerNode.setLocalTranslation(Vector3f.ZERO);
    }

    public float getMoveSpeed() {
	return moveSpeed;
    }

    public void setWalkDirection(Vector3f walkDirection) {
	this.walkDirection = walkDirection;
    }

    public void jump() {
	// Implement the logic to make the player jump
	// This will likely involve applying an upward force to the player's movement
    }
}
