package orego.cluster;

import static orego.core.Coordinates.at;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.mcts.Lgrf2Player;
import orego.mcts.StatisticalPlayer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClusterTreeSearcherTest {

	private ClusterTreeSearcher searcher;
	
	@Mock
	private RegistryFactory factory;
	
	@Mock
	private Registry mockRegistry;
	
	@Mock
	private SearchController mockController;
	
	@Mock
	private StatisticalPlayer player;
	
	@Before
	public void setup() throws Exception {
		when(factory.getRegistry()).thenReturn(mockRegistry);
		when(factory.getRegistry((String)any())).thenReturn(mockRegistry);
		when(mockRegistry.lookup(eq(ClusterPlayer.SEARCH_CONTROLLER_NAME))).thenReturn(mockController);
		
		ClusterTreeSearcher.factory = factory;
		ClusterPlayer.factory = factory;
		
		// now we setup the mock cluster player
		doNothing().when(mockController).addSearcher(any(TreeSearcher.class));
				
		// start it with the default player index (no indexing, just straight name)
		searcher = new ClusterTreeSearcher(mockController, -1);
		
		// make certain we add ourselves
		verify(mockController).addSearcher(searcher);

		searcher.setPlayer(player);
		
		// now we make certain to capture playout results
		// TODO: if I had more time, I would like to check the null edge case guards in the methods
	}
	
	@Test
	public void testShouldRequestProperClusterPlayerWithPlayerIndex() throws Exception {
		when(mockRegistry.lookup(eq(ClusterPlayer.SEARCH_CONTROLLER_NAME + "4"))).thenReturn(mockController);
		
		searcher = ClusterTreeSearcher.connectToRMI("192.random.123", 4);
		assertEquals(4, searcher.controllerIndex);
		
		verify(mockRegistry).lookup(eq(ClusterPlayer.SEARCH_CONTROLLER_NAME + "4"));
	}
	@Test
	public void testShouldResetOnPlayer() throws Exception {
		searcher.reset();
		
		verify(player).reset();
	}
	
	@Test
	public void testShouldGetBestMoveFromPlayer() throws Exception {
		long[] wins = new long[4];
		long[] runs = new long[4];
		
		when(player.getBoardWins()).thenReturn(wins);
		when(player.getBoardPlayouts()).thenReturn(runs);
		
		when(player.bestMove()).thenReturn(at("a8"));
		
		searcher.beginSearch();
		
		// wait a bit for the background thread to work
		Thread.sleep(100);
		
		// did we call back to the controller?
		verify(mockController).acceptResults(searcher, runs, wins);
	}
	
	@Test
	public void testShouldAddItselfToClusterPlayer() throws Exception {
		verify(mockController).addSearcher(searcher);
	}
	
	@Test
	public void testShouldWaitBeforeAttemptingReconnect() throws Exception {
		// make certain to fail
		when(mockRegistry.lookup(any(String.class))).thenThrow(new RemoteException());
		
		long startTime = System.currentTimeMillis();
		
		SearchController controller = ClusterTreeSearcher.tryToConnectToController(mockRegistry, 3, 1000 * 3); // three second timeout
		
		// we subtract 50 as a variation parameter to make certain we waited the appropriate amount of time
		assertTrue(System.currentTimeMillis() - startTime >= 1000 * 3 - 50);
		
		assertNull(controller);
	
	}
	
	@Test
	public void testShouldProperlyConnectToRegistry() throws Exception {
		when(mockRegistry.lookup(eq(ClusterPlayer.SEARCH_CONTROLLER_NAME + "3"))).thenReturn(mockController);
		
		SearchController controller  = ClusterTreeSearcher.tryToConnectToController(mockRegistry, 3, 1000 * 3); // three second timeout
		
		assertSame(mockController, controller);
	}
	@Test
	public void testShouldInitializeProperSubclassUsingReflection() {
		searcher.setPlayer(Lgrf2Player.class.getName());
		
		assertNotNull(searcher.getPlayer());
		assertTrue(searcher.getPlayer() instanceof Lgrf2Player);
	}
	
	@Test
	public void testShouldSetKomi() throws Exception {
		searcher.setKomi(5.0);
		
		verify(player).setKomi(5.0);
	}
	
	@Test
	public void testShouldSetPropertyOnPlayer() throws Exception {
		String key = "ponder";
		String value = "true";
		
		searcher.setProperty(key, value);
		
		verify(player).setProperty(key, value);
	}
	
	@Test
	public void testShouldAcceptMoveOnPlayer() throws Exception {
		searcher.acceptMove(0, at("a2"));
		
		verify(player).acceptMove(at("a2"));
	}

}
