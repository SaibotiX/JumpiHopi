package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.material.Material;
import java.util.Random;

public class HelloNode extends SimpleApplication {
    private Node player;
    private Node creatures;
    private float playerKarma = 0f; // Track player's moral choices
    private Random random = new Random();

    public static void main(String[] args) {
        HelloNode app = new HelloNode();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialize basic scene
        initTerrain();
        initPlayer();
        initCreatures();
        
        // Setup camera to follow player
        cam.setLocation(new Vector3f(0, 10, -10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    private void initTerrain() {
        try {
            // Create procedural terrain
            HillHeightMap heightmap = new HillHeightMap(64, 1000, 50, 100, (byte)3);
            heightmap.load();
            
            TerrainQuad terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
            Material mat = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
            terrain.setMaterial(mat);
            rootNode.attachChild(terrain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayer() {
        // Create simple player model
        player = new Node("player");
        Box box = new Box(0.5f, 1f, 0.5f);
        Geometry playerGeom = new Geometry("playerGeom", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        playerGeom.setMaterial(mat);
        player.attachChild(playerGeom);
        rootNode.attachChild(player);
    }

    private void initCreatures() {
        creatures = new Node("creatures");
        rootNode.attachChild(creatures);
        
        // Spawn some random creatures
        for (int i = 0; i < 5; i++) {
            spawnCreature();
        }
    }

    private void spawnCreature() {
        Node creature = new Node("creature");
        Box box = new Box(0.3f, 0.3f, 0.3f);
        Geometry creatureGeom = new Geometry("creatureGeom", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // Randomly determine if creature is friendly
        boolean isFriendly = random.nextBoolean();
        mat.setColor("Color", isFriendly ? ColorRGBA.Green : ColorRGBA.Red);
        creatureGeom.setMaterial(mat);
        
        // Random position
        float x = random.nextFloat() * 20 - 10;
        float z = random.nextFloat() * 20 - 10;
        creature.setLocalTranslation(x, 0, z);
        
        // Add custom user data for game logic
        creature.setUserData("friendly", isFriendly);
        
        creatures.attachChild(creature);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Basic game loop update
        updatePlayer(tpf);
        updateCreatures(tpf);
        checkInteractions();
    }

    private void updatePlayer(float tpf) {
        // Basic WASD movement (to be implemented)
    }

    private void updateCreatures(float tpf) {
        // Basic AI movement and behavior (to be implemented)
    }

    private void checkInteractions() {
        // Check for player-creature interactions
        for (int i = 0; i < creatures.getChildren().size(); i++) {
            Node creature = (Node)creatures.getChild(i);
            
            // Simple distance-based interaction
            if (creature.getLocalTranslation().distance(player.getLocalTranslation()) < 2f) {
                handleCreatureInteraction(creature);
            }
        }
    }

    private void handleCreatureInteraction(Node creature) {
        boolean isFriendly = creature.getUserData("friendly");
        if (isFriendly) {
            // Attacking a friendly creature reduces karma
            playerKarma -= 0.1f;
            System.out.println("Interacted with friendly creature. Karma: " + playerKarma);
        } else {
            // Defending against hostile creatures is neutral/positive
            System.out.println("Interacted with hostile creature");
        }
    }
}
