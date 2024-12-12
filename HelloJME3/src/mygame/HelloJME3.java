package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Spatial;
import com.jme3.math.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
import com.jme3.anim.tween.action.BlendSpace;
import com.jme3.anim.tween.action.LinearBlendSpace;
import com.jme3.anim.AnimComposer;

import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.material.RenderState.BlendMode;


public class HelloJME3 extends SimpleApplication {
    public static void main(String[] args){
        HelloJME3 app = new HelloJME3();
        app.start();
    }

    private Spatial c_s;
    private Geometry circle;
    private Geometry cylinder;
    private Geometry player;
    Node walkPlayer;
    
    private Node shootables;
    private Geometry mark;
    
    private Geometry rockSphere;
    private float g = 0.9f;
    private float invert;

    private Boolean isRunning = true;

    private AnimComposer control;
    private Action advance;
    
    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
	geom.setLocalTranslation(new Vector3f(0f,0f,-10f));
        Material mat = new Material(assetManager,
				    "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

	Sphere s = new Sphere(10,10,3);
	Geometry sphere = new Geometry("Sphere", s);
	sphere.setLocalTranslation(new Vector3f(0f,5f,-10f));
	Material sphereMat = new Material(assetManager,
					  "Common/MatDefs/Misc/Unshaded.j3md");
	sphereMat.setColor("Color", ColorRGBA.Red);
	sphere.setMaterial(sphereMat);
	rootNode.attachChild(sphere);

	Sphere c = new Sphere(5,2,3);
	circle = new Geometry("Sphere", c);
	circle.rotate(90f, 0f, 0f);
	circle.setLocalTranslation(new Vector3f(5f,0f,-10f));
	Material circleMat = new Material(assetManager,
					  "Common/MatDefs/Misc/Unshaded.j3md");
	circleMat.setColor("Color", ColorRGBA.Green);
	circle.setMaterial(circleMat);
	rootNode.attachChild(circle);
	
	Quad q = new Quad(1f, 2f);
	Geometry quad = new Geometry("Quad", q);
	quad.setLocalTranslation(new Vector3f(-4f,0f,-10f));
	Material quadMat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
	quadMat.setColor("Color", ColorRGBA.Orange);
	quad.setMaterial(quadMat);
	rootNode.attachChild(quad);

	Dome d = new Dome(2,5,5);
	Geometry dome = new Geometry("Dome", d);
	dome.setLocalTranslation(new Vector3f(-10f,0f,-10f));
	Material domeMat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
	domeMat.setColor("Color", ColorRGBA.Pink);
	dome.setMaterial(domeMat);
	rootNode.attachChild(dome);
	
	assetManager.registerLocator("/home/zuckram/jMonkeyProject/HelloJME3/assets/Models", FileLocator.class);
	c_s = assetManager.loadModel("untitled.gltf");
	c_s.setLocalTranslation(-5f,5f,-10f);

	rootNode.attachChild(c_s);
	DirectionalLight sun = new DirectionalLight();
	sun.setDirection(new Vector3f(-4.1f, 5.7f, -10.0f). normalizeLocal());
	rootNode.addLight(sun);

	Cylinder cyl = new Cylinder(5,5,3,3);
	cylinder = new Geometry("Cylinder", cyl);
	cylinder.setLocalTranslation(new Vector3f(-10f,+10f, -10f));
	Material cylMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	cylMat.setColor("Color", ColorRGBA.Blue);
	cylinder.setMaterial(cylMat);
	rootNode.attachChild(cylinder);

	Box bPl = new Box(1,1,1);
	player = new Geometry("Player", bPl);
	player.setLocalTranslation(new Vector3f(0f,0f,-20f));
	Material plMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        plMat.setColor("Color", ColorRGBA.Cyan);
	player.setMaterial(plMat);
	rootNode.attachChild(player);

	Box gBox = new Box(1,1,1);
	Geometry grassBox = new Geometry("gBox", gBox);
	Material grassMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	assetManager.registerLocator("/home/zuckram/jMonkeyProject/HelloJME3/assets/Textures", FileLocator.class);
	Texture grassTex = assetManager.loadTexture("grass.jpg");
	grassMat.setTexture("ColorMap", grassTex);
	grassBox.setMaterial(grassMat);
	rootNode.attachChild(grassBox);

	Sphere rSphere = new Sphere(50,50,3);
	rockSphere = new Geometry("rSphere", rSphere);
	rockSphere.rotate(45f,0f,0f);
	rSphere.setTextureMode(Sphere.TextureMode.Projected);
	TangentBinormalGenerator.generate(rSphere);
	rockSphere.setLocalTranslation(new Vector3f(-5f,0f,0f));
	Material rockMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
	rockMat.setTexture("DiffuseMap", assetManager.loadTexture("rocks.jpg"));
	rockMat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
	
	rockMat.setBoolean("UseMaterialColors",true);
	rockMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
	rockMat.setColor("Specular",ColorRGBA.Blue); // for shininess
	rockMat.setFloat("Shininess", 128f); // [1,128] for shininess
			   
	rockSphere.setMaterial(rockMat);
	rootNode.attachChild(rockSphere);

	initKeys();

	viewPort.setBackgroundColor(ColorRGBA.Gray);
	walkPlayer = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
	walkPlayer.setLocalScale(0.5f);
	rootNode.attachChild(walkPlayer);

	control = walkPlayer.getControl(AnimComposer.class);
	control.setCurrentAction("stand");

	BlendSpace quickBlend = new LinearBlendSpace(0f, 0.5f);
	Action halt = control.actionBlended("halt", quickBlend, "stand", "Walk");

	Action walk = control.action("Walk");
	Tween doneTween = Tweens.callMethod(this, "onAdvanceDone");
	advance = control.actionSequence("advance", walk, halt, doneTween);

	//	initCrossHairs();
	//	initMark();

	shootables = new Node("Shootables");

    }

    void onAdvanceDone(){
	control.setCurrentAction("stand");
    }
    
    private void initKeys() {
        /* You can map one or several inputs to one named mapping. */
	inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W)); 
	inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
	inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
	inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));

	inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
	inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_SPACE),
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
	
	// Listeners
//	inputManager.addListener(actionListener, "Pause");
//	inputManager.addListener(analogListener, "Left", "Right", "Rotate", "Backward", "Forward", "changeViewLeft", "changeViewRight", "changeViewUp", "changeViewDown");
	/*----------------------------------------------------------------*/

	inputManager.addMapping("Walk", new KeyTrigger(KeyInput.KEY_SPACE));

	ActionListener handler = new ActionListener(){
		@Override
		public void onAction(String name, boolean keyPressed, float tpf){
		    if(keyPressed && control.getCurrentAction() != advance){
			control.setCurrentAction("advance");
		    }
		}
	    };
	inputManager.addListener(handler, "Walk");
    }

    /** Use this listener for KeyDown/KeyUp events */
//    final private ActionListener actionListener = new ActionListener() {
//	    @Override
//	    public void onAction(String name, boolean keyPressed, float tpf) {
//		if (name.equals("Pause") && !keyPressed) {
//		    isRunning = !isRunning;
//		}
//	    }
//	};

    final private AnalogListener analogListener = new AnalogListener() {
	    @Override
	    public void onAnalog(String name, float value, float tpf) {
		if (isRunning) {
		    if (name.equals("Rotate")) {
			player.rotate(0, value, 0);
		    }
		    if (name.equals("Right")) {
			player.move((new Vector3f(value, 0,0)) );
		    }
		    if (name.equals("Left")) {
			player.move(new Vector3f(-value, 0,0));
		    }
		    if (name.equals("Forward")){
			player.move(new Vector3f(0,0,-value));
		    }
		    if (name.equals("Backward")){
			player.move(new Vector3f(0,0,value));
		    }
		} else {
		    System.out.println("Press P to unpause.");
		}
	    }
	};
    
    @Override
    public void simpleUpdate(float tpf){
	c_s.rotate(-2*tpf, 2*tpf, 0f);
	circle.rotate(tpf,2*tpf,0f);
	cylinder.rotate(0f,2*tpf,0f);
	rockSphere.rotate(0f,1*tpf,0f);
	
	Vector3f scale = c_s.getLocalScale();

	float b = scale.getY();

	if(b < 0.1){
	    g = 1.05f;

	    Material mat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
	    mat.setColor("Color", ColorRGBA.Green);
	    c_s.setMaterial(mat);

	}

	if(b > 0.9){
	    g = 0.95f;

	    Material mat = new Material(assetManager,
					"Common/MatDefs/Misc/Unshaded.j3md");
	    mat.setColor("Color", ColorRGBA.Blue);
	    c_s.setMaterial(mat);

	}
	
	c_s.scale(g,g,g);
	
    }
}
