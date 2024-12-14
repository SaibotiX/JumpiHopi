package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.light.DirectionalLight;
import com.jme3.texture.Texture;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.input.KeyInput;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;

import java.lang.System;
    
public class JumpyHopi extends SimpleApplication
    implements ActionListener{

    private Node player;
    private CharacterControl character;

    private Node grubo;
    private CharacterControl charGrubo;
    
    private float airTime = 100;
    
    Spatial startGround;
    Spatial nextGround;
    private boolean left = false, right = false, up = false, down = false;

    final private Vector3f walkDirection = new Vector3f();

    private BulletAppState bulletAppState;
    
    public static void main(String[] args){
	JumpyHopi app = new JumpyHopi();
	app.start();
    }
    
    @Override
    public void simpleInitApp() {
	bulletAppState = new BulletAppState();
	bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
	
	setupWindow();
	initObjects();
	setPhysicsObjects();
	setupKeys();
	createCharacter();
	initEnemies();
	setupChaseCamera();
	initRotate();
	setUpLight();
	addRootNode();
	
    }

    private void setupWindow(){
	String homePath = System.getProperty("user.home");
	String AssetsPath = homePath + "/JumpiHopi/HelloJME3/assets";

	assetManager.registerLocator(AssetsPath, FileLocator.class);
	
	viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
    }
    
    private void initObjects(){
	/*Make Grass Ground*/
	Box startGMesh = new Box(10f,0.2f,10f);
	startGround = new Geometry("Box", startGMesh);
	startGround.setLocalTranslation(new Vector3f(1,-5,5));

	Box nextGMesh = new Box(10f,0.2f,10f);
	nextGround = new Geometry("Box", nextGMesh);
	nextGround.setLocalTranslation(new Vector3f(1,-5,-20));
	
	Material startGroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	Texture grassTex = assetManager.loadTexture("Textures/grass.jpg");
	startGroundMat.setTexture("ColorMap", grassTex);
	
	startGround.setMaterial(startGroundMat);
	nextGround.setMaterial(startGroundMat);
    }

    private void initEnemies(){
	CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 0.5f, 1);
	charGrubo = new CharacterControl(capsule, 0.01f);

	grubo = (Node) assetManager.loadModel("Models/grubo.gltf");
	grubo.scale(2f,2f,2f);
	grubo.addControl(charGrubo);
	
	charGrubo.setPhysicsLocation(new Vector3f(0,0,0));
	getPhysicsSpace().add(charGrubo);
    }

    private void setPhysicsObjects(){
	CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(startGround);
	RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
	startGround.addControl(landscape);
	bulletAppState.getPhysicsSpace().add(landscape);

	CollisionShape nextGroundShape = CollisionShapeFactory.createMeshShape(nextGround);
	RigidBodyControl landscapeNextGround = new RigidBodyControl(nextGroundShape, 0);
	nextGround.addControl(landscapeNextGround);
	bulletAppState.getPhysicsSpace().add(landscapeNextGround);
    }

    private void setupKeys(){
	inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
	inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
	inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
	inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
	inputManager.addMapping("CharSpace", new KeyTrigger(KeyInput.KEY_SPACE));

	inputManager.addListener(this, "CharLeft");
	inputManager.addListener(this, "CharRight");
	inputManager.addListener(this, "CharUp");
	inputManager.addListener(this, "CharDown");
	inputManager.addListener(this, "CharSpace");
    }

    @Override
    public void onAction(String binding, boolean value, float tpf){
	if (binding.equals("CharLeft")) {
            if (value) {
                left = true;
            } else {
                left = false;
            }
        } else if (binding.equals("CharRight")) {
            if (value) {
                right = true;
            } else {
                right = false;
            }
        } else if (binding.equals("CharUp")) {
            if (value) {
                up = true;
            } else {
                up = false;
            }
        } else if (binding.equals("CharDown")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        } else if (binding.equals("CharSpace")) {
            character.jump();
	}

	character.setWalkDirection(walkDirection);
    }

    private void setupChaseCamera(){
	flyCam.setEnabled(false);
	new ChaseCamera(cam, player, inputManager);
    }

    private void createCharacter(){
	CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 0.5f, 1);
	character = new CharacterControl(capsule, 0.01f);

	player = (Node) assetManager.loadModel("Models/player.gltf");
	player.addControl(character);
	character.setPhysicsLocation(new Vector3f(0,0,0));
	getPhysicsSpace().add(character);
    }

    private PhysicsSpace getPhysicsSpace(){
	return bulletAppState.getPhysicsSpace();
    }
    
    private void initRotate(){
	float radian = FastMath.PI * 1.5f;
	
	player.rotate(0f,radian,0f);
	player.setLocalTranslation(0f,-3f,-2f);
	
		
    }
    
    private void setUpLight(){
	DirectionalLight dirLight = new DirectionalLight();

	dirLight.setColor(ColorRGBA.White);
	dirLight.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
	rootNode.addLight(dirLight);
    }

    private void addRootNode(){
	rootNode.attachChild(player);
	rootNode.attachChild(grubo);
	rootNode.attachChild(startGround);
	rootNode.attachChild(nextGround);
    }
    
    @Override
    public void simpleUpdate(float tpf){
	Vector3f camDir = cam.getDirection().clone().multLocal(0.1f);
	Vector3f camLeft = cam.getLeft().clone().multLocal(0.1f);
	camDir.y = 0;
	camLeft.y = 0;
	walkDirection.set(0,0,0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        if (!character.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0;
        }
	
	character.setWalkDirection(walkDirection);
	    
    }
}
