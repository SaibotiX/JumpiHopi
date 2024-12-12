package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class GameMain extends SimpleApplication implements ActionListener {
    private BulletAppState bulletAppState;
    private Player player;
    private TerrainManager terrainManager;
    private CreatureManager creatureManager;
    private BuildingSystem buildingSystem;
    private InventorySystem inventorySystem;
    private QuestManager questManager;
    
    // Movement vectors
    private Vector3f walkDirection = new Vector3f();
    private Vector3f viewDirection = new Vector3f();
    private boolean left = false, right = false, forward = false, backward = false;
    
    public static void main(String[] args) {
        GameMain app = new GameMain();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialize physics - THIS IS CRUCIAL FOR MOVEMENT
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // Initialize game systems
        initManagers();
        initInput();
        setupCamera();
    }

    private void initManagers() {
        terrainManager = new TerrainManager(this);
        creatureManager = new CreatureManager(this);
        buildingSystem = new BuildingSystem(this);
        inventorySystem = new InventorySystem();
        questManager = new QuestManager();
        player = new Player(this);
        
        // Initial world setup
        terrainManager.generateTerrain();
        creatureManager.spawnInitialCreatures(10);
    }

    private void initInput() {
        // Clear any existing mappings
        inputManager.clearMappings();
        
        // Movement mappings
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        
        // Additional action mappings
        inputManager.addMapping("Inventory", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Build", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));
        
        // Add listeners for all actions
        inputManager.addListener(this, 
            "Forward", "Backward", "Left", "Right", "Jump",
            "Inventory", "Build", "Interact");
        
        // Mouse settings
        flyCam.setEnabled(true);
        flyCam.setDragToRotate(false);
        inputManager.setCursorVisible(false);
    }

    private void setupCamera() {
        // Configure camera
        flyCam.setEnabled(true);
        flyCam.setDragToRotate(false);
        flyCam.setMoveSpeed(0); // Disable default WASD camera movement
        
        // Set initial camera position
        cam.setLocation(new Vector3f(0, 5, -10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Update movement direction based on current input
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(-1, 0, 0);
        }
        if (right) {
            walkDirection.addLocal(1, 0, 0);
        }
        if (forward) {
            walkDirection.addLocal(0, 0, 1);
        }
        if (backward) {
            walkDirection.addLocal(0, 0, -1);
        }
        
        // Normalize the walk direction to prevent faster diagonal movement
        if (walkDirection.lengthSquared() > 0) {
            walkDirection.normalizeLocal();
            walkDirection.multLocal(player.getMoveSpeed());
        }
        
        // Update game systems
        player.setWalkDirection(walkDirection);
        player.update(tpf);
        creatureManager.update(tpf);
        questManager.update(tpf);
        updateCamera();
    }

    private void updateCamera() {
        Vector3f playerPos = player.getPosition();
        // Adjust these values to change camera distance and height
        cam.setLocation(playerPos.add(new Vector3f(0, 5, -10)));
        cam.lookAt(playerPos, Vector3f.UNIT_Y);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        // Handle movement inputs
        switch (name) {
            case "Left":
                left = isPressed;
                break;
            case "Right":
                right = isPressed;
                break;
            case "Forward":
                forward = isPressed;
                break;
            case "Backward":
                backward = isPressed;
                break;
            case "Jump":
                if (isPressed) {
                    player.jump();
                }
                break;
        }
        
        // Handle other actions when key is pressed
        if (isPressed) {
            switch (name) {
                case "Inventory":
                    inventorySystem.toggle();
                    break;
                case "Build":
                    buildingSystem.toggleBuildMode();
                    break;
                case "Interact":
                    handleInteraction();
                    break;
            }
        }
    }

    private void handleInteraction() {
        Vector3f playerPos = player.getPosition();
        creatureManager.checkInteractions(playerPos);
        buildingSystem.checkInteraction(playerPos);
    }

    // Getter methods
    public BulletAppState getBulletAppState() { return bulletAppState; }
    public TerrainManager getTerrainManager() { return terrainManager; }
    public CreatureManager getCreatureManager() { return creatureManager; }
    public BuildingSystem getBuildingSystem() { return buildingSystem; }
    public InventorySystem getInventorySystem() { return inventorySystem; }
    public QuestManager getQuestManager() { return questManager; }
}
