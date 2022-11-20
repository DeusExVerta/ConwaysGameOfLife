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
						if(sb.length()<size) {
							sb.reverse();
							while(sb.length()<size) 
							{
								sb.append('0');
							}
							sb.reverse();
							return sb.toString();
						}
						return sb.substring(sb.length()-size, sb.length());
					}
				).iterator();
		
		current=new ArrayList<List<Boolean>>(size);
		for(int i=0;i<size;i++) 
		{
			String s; 
			if(iter.hasNext()) 
			{ 
				s = iter.next();
			}else{
				StringBuilder sb = new StringBuilder();
				while(sb.length()<size) 
				{
					sb.append(0);
				}
				s = sb.toString();
			}
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
	
	//TODO: Parallelize 
	public void nextGeneration() 
	{
		log.info("Starting Calculation of Next Generation");
		//compute next generation from current
		this.next = IntStream.range(0, size)
				.parallel()
				.mapToObj(
					(i)->IntStream.range(0, size)
					.parallel()
					.mapToObj(
							(j)->getNextCellState(i,j)
							).toList()
					).toList();
		log.info("Finished Calculating Next Generation");
		//increment generation
		generation++;
		//swap references of current and next after completing computation of next
		List<List<Boolean>> tmp = current;
		current=next;
		next = tmp;
	}
	
	private Boolean getNextCellState(Integer i, Integer j)
	{
		Integer result = IntStream.range(-1, 2)
				.parallel()
				.map(
				(k)->IntStream.range(-1, 2)
					.mapToObj(
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
		log.debug("Cell:["+i+","+j+"]");
		return Game.liveSum(result, current.get(i).get(j));
	}
	
	private static Boolean liveSum(Integer sum, Boolean state) 
	{
		Boolean result = sum==3||(state&&sum==2);
		log.debug(String.format("[Sum:%d, State:%b, Next State:%b]",sum,state,result));
		return result;
	}
	
	//if at least 1 cell is live and the previous state is not the same as the current state the game is live
	public boolean isLive()
	{
		return hasLiveCells() && !currentIsNext();
	}
	
	private Boolean hasLiveCells() 
	{
		log.info("Testing for Live Cells");
		for(List<Boolean> list:current) 
		{
			for(Boolean b:list) 
			{
				if(b)
				{
					log.info(String.format("%s Result: %b",Thread.currentThread().getStackTrace()[1].getMethodName(), true));
					return true;
				}
				log.debug("Dead Cell. Continuing");
			}
			log.debug("Row Complete. Continuing");
		}
		log.info(String.format("%s Result: %b",Thread.currentThread().getStackTrace()[1].getMethodName(), false));
		return false;
//		Boolean result = current.parallelStream().map(
//				(list)->list.parallelStream()
//				.reduce(
//						false,
//						(rowResult,cell)->{
//							log.debug(String.format("Cell Value: %b, HasTrue: %b",cell, rowResult));
//							return cell||rowResult;
//							}
//						)
//				)
//			.reduce(
//					false,
//					(isLive,rowTotal)->{
//						log.debug(String.format("Row Value: %b, HasTrue: %b",rowTotal,isLive));
//						return isLive||rowTotal;
//						}
//					);
//		log.debug(String.format("%s Result: %b",Thread.currentThread().getStackTrace()[1].getMethodName(), result));
//		return result;
	}
	
	//return true iff all rows are true
	private Boolean currentIsNext() 
	{
		log.info("Testing if current state equals next state");
		for(int i=0;i<size;i++) 
		{
			for(int j=0;j<size;j++) 
			{
				if(current.get(i).get(j)!=next.get(i).get(j)) 
				{
					log.info(String.format("%s Result: %b",Thread.currentThread().getStackTrace()[1].getMethodName(), false));
					return false;
				}
			}
		}
		log.info(String.format("%s Result: %b",Thread.currentThread().getStackTrace()[1].getMethodName(), true));
		return true;
//		Boolean result = IntStream.range(0, size)
//				.mapToObj(
//						(i)->{
//						Boolean cur = current.get(i).equals(next.get(i));
//						log.debug(String.format("Current %s, Next %s Equals: %b",current.get(i),next.get(i), cur));
//						return cur;
//						}
//						)
//				.reduce(true,
//						(equals,rowResult)->{
//							log.debug(String.format("Equals %b, RowResult %b", equals, rowResult));
//							return equals&&rowResult;}
//						);
//		log.debug(String.format("%s Result: %b",Thread.currentThread().getStackTrace()[1].getMethodName(), result));
//		return result;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Generation ");
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
		return sb.toString();
	}
}
