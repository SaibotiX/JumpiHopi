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

import com.jme3.input.controls.ActionListener;

public class JumpyHopi extends SimpleApplication
    implements ActionListener{

    Node
	player;
    Spatial startGround;
    
    public static void main(String[] args){
	JumpyHopi app = new JumpyHopi();
	app.start();
    }
    
    @Override
    public void simpleInitApp() {
	assetManager.registerLocator("/home/zuckram/jMonkeyProject/HelloJME3/assets/Textures", FileLocator.class);
	assetManager.registerLocator("/home/zuckram/jMonkeyProject/HelloJME3/assets/Models", FileLocator.class);
	viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

	initObjects();
	
	player = (Node) assetManager.loadModel("player.gltf");

	initRotate();
	setUpLight();
	addRootNode();
	
    }

    private void initObjects(){
	/*Make Grass Ground*/
	Box startGMesh = new Box(10f,0.2f,10f);
	startGround = new Geometry("Box", startGMesh);
	startGround.setLocalTranslation(new Vector3f(1,-5,5));
	Material startGroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	assetManager.registerLocator("/home/zuckram/jMonkeyProject/HelloJME3/assets/Textures", FileLocator.class);
	Texture grassTex = assetManager.loadTexture("grass.jpg");
	startGroundMat.setTexture("ColorMap", grassTex);
	startGround.setMaterial(startGroundMat);

	/* */
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
	rootNode.attachChild(startGround);	
    }
    
    @Override
    public void simpleUpdate(float tpf){
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
    }
}
