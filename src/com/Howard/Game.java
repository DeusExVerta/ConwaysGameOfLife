package com.Howard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;





public class Game {
	//reference variables for each generation.
	private List<List<Boolean>> current, next;
	private int generation=0;
	private final int size;
	private static final Logger log = LogManager.getLogger(Game.class);
	public static enum Construct
	{
		GLIDER(new int[] {5,6,2}),
		BOX(new int[] {2,5,2});
		private List<Integer> layout;
		private Construct(int[] ints) 
		{
			layout=new ArrayList<Integer>(ints.length);
			for(int val:ints) 
			{
				layout.add(val);
			}
		}
		public List<Integer> getLayout() 
		{
			return this.layout;
		}
	}
	
	public Game(int size) 
	{
		this.size=size;
		this.current=initState((int i)->false);
		this.next=initState((int i)->false);
	}
	
	/* constructor taking a single integer size and a List of Integers
	 * the last {size} bits of the binary representation of those numbers
	 * are used to specify the state of the cells in each row 
	*/
	public Game(int size, List<Integer> ints) 
	{
		this.size=size;
		
		Iterator<String> iter = ints.stream().map(
				(x)->{
					StringBuilder sb = new StringBuilder(
						Integer.toBinaryString(x)
						);
						sb.reverse();
						while(sb.length()<32) 
						{
							sb.append('0');
						}
						sb.reverse();
						return sb.toString().substring(32-size,32);
					}
				).iterator();
		
		current=new ArrayList<List<Boolean>>(size);
		for(int i=0;i<size;i++) 
		{
			String s = iter.hasNext()?iter.next():"00000000000000000000000000000000";
			ArrayList<Boolean> row=new ArrayList<Boolean>(size);
			for(int j=0;j<size;j++){
				row.add(s.charAt(j)=='1');
			}
			current.add(row);
		}
		this.next=initState((int i)->false);
	}
	
	//builder method for a game of a specified size with a glider configuration in the top right
	public static Game gliderGame(int size)
	{
		Game game;
		List<Integer> layout = Construct.GLIDER.getLayout();
		if(size<5){
			game=new Game(10, layout);
		}
		else
		{
			game = new Game(size, layout);
		}
		return game;
	}
	
	//builder method for a game with random initial state
	public static Game randomGame(int size) 
	{
		Random random = new Random();
		Game game = new Game(size);
		game.current.forEach((li)->li.stream().map((x)->random.nextBoolean()));
		return game;
	}
		
	private List<List<Boolean>> initState(IntFunction<Boolean> function) 
	{
		return IntStream.range(0, size)
				.mapToObj(
						(i)->(IntStream.range(0,size)
								.mapToObj(
										(j)->(function.apply(j))
										)
								.toList()
								)
						)
				.toList();
	}
	
	public List<List<Boolean>> getGeneration() 
	{
		return current;
	}
	
	public void nextGeneration() 
	{
		//compute next generation from current
		this.next = IntStream.range(0, size)
				.mapToObj(
					(i)->IntStream.range(0, size)
					.mapToObj(
							(j)->getNextCellState(i,j)
							).toList()
					).toList();
		//increment generation
		generation++;
		//swap references of current and next after completing computation of next
		List<List<Boolean>> tmp = current;
		current=next;
		next = tmp;
	}
	
	private Boolean getNextCellState(Integer i, Integer j)
	{
		Integer result = IntStream.range(-1, 2).map(
				(k)->IntStream.range(-1, 2).mapToObj(
						(q)->{
							Boolean b = current
								.get(
									(i+k+size)%size
										)
								.get(	
									(j+q+size)%size
										) && ! (k==0&&q==0);
							
							return b;
							}
				).reduce(0,(acc,cur)->(acc+(cur?1:0)),Integer::sum)
				).reduce(0,(acc2,cur2)->(acc2+cur2));
		log.info("Cell:["+i+","+j+"]");
		return Game.liveSum(result, current.get(i).get(j));
	}
	
	private static Boolean liveSum(Integer sum, Boolean state) 
	{
		Boolean result = sum==3||(state&&sum==2);
		log.info(String.format("[Sum:%d, State:%b, Next State:%b]",sum,state,result));
		return result;
	}
	
	//if at least 1 cell is live and the previous state is not the same as the current state the game is live
	public boolean isLive()
	{
		return hasLiveCells() && !currentIsNext();
	}
	
	//TODO: examine if traditional loops are faster due to the ability to short circuit 
	private Boolean hasLiveCells() 
	{
		return current.parallelStream().map(
				(list)->list.stream()
				.reduce(
						false,
						(rowResult,cell)->(cell||rowResult)
						)
				)
			.reduce(
					false,
					(isLive,rowTotal)->(isLive||rowTotal)
					);
	}
	
	//TODO: examine if traditional loops are faster due to the ability to short circuit 
	private Boolean currentIsNext() 
	{
		return IntStream.range(0, size).mapToObj((i)->current.get(i).equals(next.get(i)))
				.reduce(false,
						(overall,rowResult)->(overall||rowResult)
						);
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder("Generation ");
		sb.append(generation);
		current.forEach(
				(li)->{
					sb.append("\n\t");
					li.forEach(
							(b)->{
								sb.appendCodePoint(b?0x2588:0x2591);
							}
						);
					}
				);
		sb.append("\nGame Live:");
		sb.append(isLive());
		return sb.toString();
	}
}
