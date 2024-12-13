package mygame;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.Armature;
import com.jme3.anim.ArmatureMask;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A walking animated character followed by a 3rd person camera on a terrain with LOD.
 * @author normenhansen
 */
public class movingAnimatedCharacter extends SimpleApplication
        implements ActionListener, PhysicsCollisionListener {

    private BulletAppState bulletAppState;
    //character
    private CharacterControl character;
    private Node model;
    //temp vectors
    final private Vector3f walkDirection = new Vector3f();
    //Materials
    private Material matBullet;
    //animation
    private Action standAction;
    private Action walkAction;
    private AnimComposer composer;
    private float airTime = 100;
    //camera
    private boolean left = false, right = false, up = false, down = false;
    //bullet
    private Sphere bullet;
    private SphereCollisionShape bulletCollisionShape;
    //explosion
    private ParticleEmitter effect;
    //brick wall
    private Box brick;
    final private float bLength = 0.8f;
    final private float bWidth = 0.4f;
    final private float bHeight = 0.4f;

    public static void main(String[] args) {
        movingAnimatedCharacter app = new movingAnimatedCharacter();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
        setupKeys();
        prepareBullet();
        prepareEffect();
        createLight();
        createSky();
        createTerrain();
        createWall();
        createCharacter();
        setupChaseCamera();
        setupAnimationController();
        setupFilter();
    }

    private void setupFilter() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("CharSpace", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("CharShoot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "CharLeft");
        inputManager.addListener(this, "CharRight");
        inputManager.addListener(this, "CharUp");
        inputManager.addListener(this, "CharDown");
        inputManager.addListener(this, "CharSpace");
        inputManager.addListener(this, "CharShoot");
    }

        @Override
    public void onAction(String binding, boolean value, float tpf) {
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
        } else if (binding.equals("CharShoot") && !value) {
            bulletControl();
        }
    }

        @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(10f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(10f);
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);
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

        Action action = composer.getCurrentAction("walkingLayer");
        if (walkDirection.length() == 0f) {
            if (action != standAction) {
                composer.setCurrentAction("stand", "walkingLayer");
            }
        } else {
            character.setViewDirection(walkDirection);
            if (airTime > 0.3f) {
                if (action != standAction) {
                    composer.setCurrentAction("stand", "walkingLayer");
                }
            } else if (action != walkAction) {
                composer.setCurrentAction("Walk", "walkingLayer");
            }
        }
        character.setWalkDirection(walkDirection);
    }



    private void setupChaseCamera() {
        flyCam.setEnabled(false);
        new ChaseCamera(cam, model, inputManager);
    }

        private void createCharacter() {
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
        character = new CharacterControl(capsule, 0.01f);
        model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        model.addControl(character);
        character.setPhysicsLocation(new Vector3f(-140, 40, -10));
        rootNode.attachChild(model);
        getPhysicsSpace().add(character);
    }
    
    private void createWall() {
        float xOff = -144;
        float zOff = -40;
        float startpt = bLength / 4 - xOff;
        float height = 6.1f;
        brick = new Box(bLength, bHeight, bWidth);
        brick.scaleTextureCoordinates(new Vector2f(1f, .5f));
        for (int j = 0; j < 15; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt = new Vector3f(i * bLength * 2 + startpt, bHeight + height, zOff);
                addBrick(vt);
            }
            startpt = -startpt;
            height += 1.01f * bHeight;
        }
    }

    private void addBrick(Vector3f ori) {
        Geometry brickGeometry = new Geometry("brick", brick);
        brickGeometry.setMaterial(matBullet);
        brickGeometry.setLocalTranslation(ori);
        brickGeometry.addControl(new RigidBodyControl(1.5f));
        brickGeometry.setShadowMode(ShadowMode.CastAndReceive);
        this.rootNode.attachChild(brickGeometry);
        this.getPhysicsSpace().add(brickGeometry);
    }

    private void prepareBullet() {
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.4f);
        matBullet = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBullet.setColor("Color", ColorRGBA.Green);
        matBullet.setColor("GlowColor", ColorRGBA.Green);
        getPhysicsSpace().addCollisionListener(this);
    }

    private void prepareEffect() {
        int COUNT_FACTOR = 1;
        float COUNT_FACTOR_F = 1f;
        effect = new ParticleEmitter("Flame", Type.Triangle, 32 * COUNT_FACTOR);
        effect.setSelectRandomImage(true);
        effect.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (1f / COUNT_FACTOR_F)));
        effect.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        effect.setStartSize(1.3f);
        effect.setEndSize(2f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        effect.setParticlesPerSec(0);
        effect.setGravity(0, -5, 0);
        effect.setLowLife(.4f);
        effect.setHighLife(.5f);
        effect.getParticleInfluencer()
                .setInitialVelocity(new Vector3f(0, 7, 0));
        effect.getParticleInfluencer().setVelocityVariation(1f);
        effect.setImagesX(2);
        effect.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        effect.setMaterial(mat);
//        effect.setLocalScale(100);
        rootNode.attachChild(effect);
    }

    private void createLight() {
        Vector3f direction = new Vector3f(-0.1f, -0.7f, -1).normalizeLocal();
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(direction);
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);
    }

    private void createSky() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, 
                "Textures/Sky/Bright/BrightSky.dds", 
                SkyFactory.EnvMapType.CubeMap));
    }

    private void createTerrain() {
        Material matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);
        matRock.setBoolean("WardIso", true);
        matRock.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("DiffuseMap", grass);
        matRock.setFloat("DiffuseMap_0_scale", 64);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("DiffuseMap_1", dirt);
        matRock.setFloat("DiffuseMap_1_scale", 16);
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("DiffuseMap_2", rock);
        matRock.setFloat("DiffuseMap_2_scale", 128);
        Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
        normalMap0.setWrap(WrapMode.Repeat);
        Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
        normalMap1.setWrap(WrapMode.Repeat);
        Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
        normalMap2.setWrap(WrapMode.Repeat);
        matRock.setTexture("NormalMap", normalMap0);
        matRock.setTexture("NormalMap_1", normalMap1);
        matRock.setTexture("NormalMap_2", normalMap2);

        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        TerrainQuad terrain
                = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        List<Camera> cameras = new ArrayList<>();
        cameras.add(getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(2, 2, 2));

        RigidBodyControl terrainPhysicsNode
                = new RigidBodyControl(CollisionShapeFactory.createMeshShape(terrain), 0);
        terrain.addControl(terrainPhysicsNode);
        rootNode.attachChild(terrain);
        getPhysicsSpace().add(terrainPhysicsNode);
    }



    private void setupAnimationController() {
        composer = model.getControl(AnimComposer.class);
        standAction = composer.action("stand");
        walkAction = composer.action("Walk");
        /*
         * Add a "shootOnce" animation action
         * that performs the "Dodge" action one time only.
         */
        Action dodgeAction = composer.action("Dodge");
        Tween doneTween = Tweens.callMethod(this, "onShootDone");
        composer.actionSequence("shootOnce", dodgeAction, doneTween);
        /*
         * Define a shooting animation layer
         * that animates only the joints of the right arm.
         */
        SkinningControl skinningControl
                = model.getControl(SkinningControl.class);
        Armature armature = skinningControl.getArmature();
        ArmatureMask shootingMask
                = ArmatureMask.createMask(armature, "uparm.right");
        composer.makeLayer("shootingLayer", shootingMask);
        /*
         * Define a walking animation layer
         * that animates all joints except those used for shooting.
         */
        ArmatureMask walkingMask = new ArmatureMask();
        walkingMask.addBones(armature, "head", "spine", "spinehigh");
        walkingMask.addFromJoint(armature, "hip.left");
        walkingMask.addFromJoint(armature, "hip.right");
        walkingMask.addFromJoint(armature, "uparm.left");
        composer.makeLayer("walkingLayer", walkingMask);

        composer.setCurrentAction("stand", "shootingLayer");
        composer.setCurrentAction("stand", "walkingLayer");
    }





    private void bulletControl() {
        composer.setCurrentAction("shootOnce", "shootingLayer");

        Geometry bulletGeometry = new Geometry("bullet", bullet);
        bulletGeometry.setMaterial(matBullet);
        bulletGeometry.setShadowMode(ShadowMode.CastAndReceive);
        bulletGeometry.setLocalTranslation(character.getPhysicsLocation().add(cam.getDirection().mult(5)));
        RigidBodyControl bulletControl = new BombControl(bulletCollisionShape, 1);
        bulletControl.setCcdMotionThreshold(0.1f);
        bulletControl.setLinearVelocity(cam.getDirection().mult(80));
        bulletGeometry.addControl(bulletControl);
        rootNode.attachChild(bulletGeometry);
        getPhysicsSpace().add(bulletControl);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (event.getObjectA() instanceof BombControl) {
            final Spatial node = event.getNodeA();
            effect.killAllParticles();
            effect.setLocalTranslation(node.getLocalTranslation());
            effect.emitAllParticles();
        } else if (event.getObjectB() instanceof BombControl) {
            final Spatial node = event.getNodeB();
            effect.killAllParticles();
            effect.setLocalTranslation(node.getLocalTranslation());
            effect.emitAllParticles();
        }
    }

    /**
     * Callback to indicate that the "shootOnce" animation action has completed.
     */
    void onShootDone() {
        /*
         * Play the "stand" animation action on the shooting layer.
         */
        composer.setCurrentAction("stand", "shootingLayer");
    }
}
