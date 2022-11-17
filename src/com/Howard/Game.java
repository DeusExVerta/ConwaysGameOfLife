package com.Howard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game {
	//reference variables for each generation.
	private List<List<Boolean>> current, next;
	private int generation=0;
	private final int size;
	
	public Game(int size) 
	{
		this.size=size;
		current=new ArrayList<List<Boolean>>(size);
		for(int i=0;i<size;i++) 
		{
			ArrayList<Boolean> row=new ArrayList<Boolean>(size);
			for(int j=0;j<size;j++){
				row.add(false);
			}
			current.add(row);
		}
		initEmptyNext();
	}
	
	/* constructor taking a single integer size and a List of Integers
	 * the last {size} bits of the binary representation of those numbers
	 * are used to specify the state of the cells in each row 
	*/
	public Game(int size, List<Integer> ints) throws Exception 
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
			String s = iter.next();
			ArrayList<Boolean> row=new ArrayList<Boolean>(size);
			for(int j=0;j<size;j++){
				row.add(s.charAt(j)=='1');
			}
			current.add(row);
		}
		initEmptyNext();
	}
	
	//constructor for a game of a specified size with a glider configuration in the top left
	public static Game gliderGame(int size)
	{
		Game game;
		if(size<4){
			game=new Game(10);
		}
		else
		{
			game = new Game(size);
		}
		game.get(0).set(2,true);
		game.get(1).set(0, true);
		game.get(1).set(2, true);
		game.get(2).set(1,true);
		game.get(2).set(2, true);
		return game;
	}
	
	public static Game randomGame(int size) 
	{
		Random random = new Random();
		Game game = new Game(size);
		game.current.forEach((li)->li.stream().map((x)->random.nextBoolean()));
		return game;
	}
	
	
	
	private List<Boolean> get(int i) 
	{
		return current.get(i);
	}
	
	
	private void initEmptyNext() 
	{
		this.next=new ArrayList<List<Boolean>>(size);
		for(int i=0;i<size;i++) 
		{
			ArrayList<Boolean> row=new ArrayList<Boolean>(size);
			for(int j=0;j<size;j++){
				row.add(false);
			}
			next.add(row);
		}
	}
	
	
	public List<List<Boolean>> getGeneration() 
	{
		return current;
	}
	
	public void nextGeneration() 
	{
		//compute next generation from current
		for(short i = 0;i<size;i++) 
		{
			for(short j=0;j<size;j++) 
			{
				short sum=0;
				for(short k=-1;k<2;k++) 
				{
					for(short q=-1;q<2;q++) 
					{
						
						if(
							!(k==0&&q==0)&&
							current
								.get((i+k+size)%size)
								.get((j+q+size)%size)
							) 
						{sum++;}
					}
				}
				//System.out.println(String.format("Cell [%d,%d] state %b, sum %d", i,j,current.get(i).get(j),sum));
				if(sum==3||(current.get(i).get(j)&&sum==2)) 
				{
					next.get(i).set(j, true);
				}else 
				{
					next.get(i).set(j, false);
				}
			}
		}
		//increment generation
		generation++;
		//swap references of current and next after completing computation of next
		List<List<Boolean>> tmp = current;
		current=next;
		next = tmp;
	}
	public boolean isLive()
	{
		return current.parallelStream().map(
				(li)->li.stream()
				.reduce(false,(isLive,b)->(b||isLive))
				)
			.reduce(
					false,
					(result,u)->(result||u)
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
		sb.append("\nLive:");
		sb.append(isLive());
		return sb.toString();
	}
}
