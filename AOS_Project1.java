import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import com.sun.nio.sctp.*;

/* author
Shreyas Lakshminarayana
*/

class AOS_Project1{
	
	public Server[] serve;						//Objects of server class
	public ArrayList<Client> intercl;				//Objects of client class
	public int n;							//Number of processes
	public int intersum;
	public int path_length;
	public ArrayList<Integer> index;				
	public ArrayList<String> node;					//Array List for host names
	public ArrayList<Integer> portnum;				//Array List for port numbers
	public ArrayList<String> pth;					//Array List for path
	public String interpath;
	public int sender;
	public int prt;
	public static final int MESSAGE_SIZE = 100;
	public Thread[] ser;
	public ArrayList<Thread> cli;
	public int cnt;
	public int exitflag;
	
	AOS_Project1()
	{
		cnt=0;
		exitflag=0;
		prt=0;
		intersum=0;
		intercl = new ArrayList<Client>();
		cli = new ArrayList<Thread>();
		index = new ArrayList<Integer>();
		path_length=0;
		node = new ArrayList<String>();
		pth = new ArrayList<String>();
		portnum = new ArrayList<Integer>();
		
		//Read the configuration file here
		File file = new File("config.txt");
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line, tempnode, IPtemp;
			int tmpport;
			while ((line = reader.readLine()) != null) 
		    	{
		    		line = line.trim();
				if(line.isEmpty())			//Skipping empty lines
				{
					continue;
				}
				else if(line.charAt(0) == '#')		//Skipping comments
				{
					continue;
				}
				else
				{
					if(line.length()==1)
					{
						n = Integer.parseInt(line);
						if(n<=0)
							System.exit(1);
					}
					else
					{
						StringTokenizer st = new StringTokenizer(line,"\n\t");
		   	 			while(st.hasMoreTokens())
		    				{
							tempnode = st.nextToken();
							tempnode += ".utdallas.edu";
							tmpport = Integer.parseInt(st.nextToken());
							node.add(tempnode);
					       	 	portnum.add(tmpport);
							if(st.hasMoreTokens())
					        		pth.add(st.nextToken());
					        	else
					        		pth.add(null);
					    	}
			    		}
				}
			}
		    	reader.close();	
			IPtemp = InetAddress.getLocalHost().getHostName();
			for(int u=0;u<node.size();u++)
			{
			       		if(IPtemp.equals(node.get(u)))
			        		index.add(u);
			}
		}			
		catch(IOException e)
		{
			System.out.println("Buffered Reader failed in Server");
		}
		ser = new Thread[index.size()];
		serve = new Server[index.size()];
		for(int s=0;s<index.size();s++)					//Creating server threads
		{
			serve[s] = new Server(s);
			ser[s] = new Thread(serve[s]);
			ser[s].start();
		}
	}
	
	class Client implements Runnable					//Client class handles sending of token to other systems
	{
		public int value;
		public int no;
		public int flag;
		public int int_sum;
		public int sendr;
		public int portno;
		public String interpath;

		Client(int val,int no,int flg)
		{
			this(val,no,flg,-1,-1,-1,null);
		}
		Client(int val, int no,int flg,int inter_sum,int sndr,int pt,String inter_pt)
		{
			this.no=no;
			flag = flg;
			value = val;
			int_sum = inter_sum;
			sendr = sndr;
			portno = pt;
			interpath = inter_pt;

		}
		public synchronized void run()
		{
			try
			{
				//Create socket depending on the path
				//System.out.println("Entered Client");
				if(flag == 1)					//To send the process’s own token
				{ 	
					flag=0;
					try
					{
						Thread.sleep(10000);
					}
					catch(Exception e)
					{
						System.out.println("Sleep of thread failed");
					}
					if(pth.get(index.get(no))!=null)
					{
						String str = pth.get(index.get(no));
						String[] nodestr=pth.get(index.get(no)).split(" ");
						int nxtnode=Integer.parseInt(nodestr[0])-1;
						ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
						SocketAddress socketAddress = new InetSocketAddress(node.get(nxtnode),portnum.get(nxtnode));
						SctpChannel sctpChannel = SctpChannel.open();
						sctpChannel.bind(new InetSocketAddress(0));
						sctpChannel.connect(socketAddress);
						MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
						String message="";
						if(str.length()>1)
						{
							String str2 = str.substring(2);
							message = value+"\t"+str2+"\t"+index.get(no)+"\t"+portnum.get(index.get(no));
						}
						else
						{
							message = value+"\t-1\t"+index.get(no)+"\t"+portnum.get(index.get(no));
						}
						byteBuffer.put(message.getBytes());
						byteBuffer.flip();
						sctpChannel.send(byteBuffer,messageInfo);
					} 
				}
				else						//To send other machines’s token
				{
					//System.out.println("Entering else loop in client");
					if(!interpath.equals("-1"))
					{
						int next = (int) (interpath.charAt(0)) - 49;
						//System.out.println("Trying to create socket with "+node.get(next));
						ByteBuffer byteBuffer1 = ByteBuffer.allocate(MESSAGE_SIZE);
						SocketAddress socketAddress = new InetSocketAddress(node.get(next),portnum.get(next));
						SctpChannel sctpChannel1 = SctpChannel.open();
						sctpChannel1.bind(new InetSocketAddress(0));
						sctpChannel1.connect(socketAddress);
						MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
						String message="";
						if(interpath.length()>1)
						{
							String str2 = interpath.substring(2);
							message = int_sum+"\t"+str2+"\t"+sendr+"\t"+portno;
						}
						else
						{
							message = int_sum+"\t-1\t"+sendr+"\t"+portno;
						}
						byteBuffer1.put(message.getBytes());
						byteBuffer1.flip();
						sctpChannel1.send(byteBuffer1,messageInfo);
						//System.out.println("Created socket to "+node.get(next)+" at port number "+portnum.get(next));
					}
					else
					{
						//System.out.println("Trying to create socket back to the sender "+node.get(sendr)+" at port number "+portno);
						ByteBuffer byteBuffer2 = ByteBuffer.allocate(MESSAGE_SIZE);
						//System.out.println(sender+" "+node.get(sendr));
						SocketAddress socketAddress = new InetSocketAddress(node.get(sendr),portno);
						SctpChannel sctpChannel2 = SctpChannel.open();
						sctpChannel2.bind(new InetSocketAddress(0));
						sctpChannel2.connect(socketAddress);
						MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
						String message = int_sum+"\t-1\t-1";
						byteBuffer2.put(message.getBytes());
						byteBuffer2.flip();
						sctpChannel2.send(byteBuffer2,messageInfo);
						//System.out.println("Created socket to "+node.get(sendr)+" at port number "+portno);
					}
					
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	class ClientWorker implements Runnable 				// Helper class for Server where most of the functions of server are handled here and server class is only responsible for creating its own socket
	{
		private SctpChannel sctpChannel;
		public int lvalue;
		public int no;
		public HashMap<String,Integer> hash;
	   
		ClientWorker(SctpChannel client,int lval,int no) 
		{
			this.sctpChannel = client;
			lvalue = lval;
			this.no = no;
			hash = new HashMap<String,Integer>();
		}	
			
		public String byteToString(ByteBuffer byteBuffer)
		{
			byteBuffer.position(0);
			byteBuffer.limit(MESSAGE_SIZE);
			byte[] bufArr = new byte[byteBuffer.remaining()];
			byteBuffer.get(bufArr);
			return new String(bufArr);
		}

		public synchronized void run()
		{
			String[] wds;
			try 
			{
				//Calculate the sum by reading the string sent by client
				ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
				MessageInfo messageInfo = sctpChannel.receive(byteBuffer,null,null);
				String str = byteToString(byteBuffer);
				//System.out.println("Input is: "+str);
				if((str.substring(0,9)).equals("I am done"))
				{
					//System.out.println(str);
					cnt++;
					if(cnt==n && exitflag ==1)
						kill();
				}
				else
				{
					wds = str.split("\t");
					int k = (int) (wds[2].charAt(0))-48;
					sender = k;
					if(sender<0)
					{
						System.out.println("The sum is: "+wds[0]);
						try
						{
							Thread.sleep(5000);
						}
						catch(Exception p)
						{
							System.out.println("Thread sleep failed");
						}
						for(int g=0;g<node.size();g++)
						{
							int p=0;
							//System.out.println("Our ip address is: "+InetAddress.getLocalHost().getHostName());
							while(p<5)
							{
								try
								{
									if(!(InetAddress.getLocalHost().getHostName().equals(node.get(g))))
									{
										if(!hash.containsKey(node.get(g)))
										{
											//System.out.println("I am trying to connect to "+node.get(g)+" at port number "+portnum.get(g));
											SocketAddress socketAddress1 = new InetSocketAddress(node.get(g),portnum.get(g));
											ByteBuffer byteBuffer13 = ByteBuffer.allocate(MESSAGE_SIZE);
											SctpChannel sctpChannel13 = SctpChannel.open();
											sctpChannel13.bind(new InetSocketAddress(0));
											sctpChannel13.connect(socketAddress1);
											MessageInfo messageInfo1 = MessageInfo.createOutgoing(null,0);
											String message="I am done from "+node.get(index.get(no))+" at port number "+portnum.get(index.get(no));
											byteBuffer13.put(message.getBytes());
											byteBuffer13.flip();
											sctpChannel13.send(byteBuffer13,messageInfo1);
											//System.out.println("Sent message to: "+node.get(g)+" at port number "+portnum.get(g));
										}
									}
								}
								catch(IOException o)
								{
									try
									{
										Thread.sleep(1000);
									}
									catch(Exception j)
									{
										System.out.println("Sleep failed");
									}
									p++;
								}
								p=6;
							}
							hash.put(node.get(g),portnum.get(g));
						}
						cnt++;
						exitflag=1;
						if(cnt==n && exitflag == 1)
							kill();
					}
					else
					{
						String pt = wds[3];
						prt = Integer.parseInt(pt.substring(0,4));
						intersum = Integer.parseInt(wds[0]);
						//System.out.println("Before addition "+intersum);
						intersum += lvalue;
						//System.out.println("After "+intersum);
						interpath = wds[1];
						//System.out.println("The path to be taken is: "+interpath);
						Client temp = new Client(lvalue,no,-1,intersum,sender,prt,interpath);
						intercl.add(temp);
						Thread l = new Thread(temp);
						cli.add(l);
						//System.out.println("Starting client from Server");
						l.start();
					}
				} 
			}
			catch (IOException e) 
			{
				System.out.println("in or out failed");
				System.exit(-1);
			}
		}
		public void kill()
		{
			System.exit(1);
		}
	}
	
	class Server implements Runnable 
	{	
		private int no;
		public int lvalue;						//Label value for that process
		Server(int ind)
		{
			no = ind;
			lvalue=(int) (Math.random() * (500));
			System.out.println("The label value for port number "+portnum.get(index.get(no))+" is "+lvalue);

		}
		public synchronized void run()
		{
			try
			{
				SctpServerChannel sctpServerChannel = SctpServerChannel.open();
				InetSocketAddress serverAddr = new InetSocketAddress(portnum.get(index.get(no)));
				sctpServerChannel.bind(serverAddr);
				System.out.println("Server running on port " + portnum.get(index.get(no)) + "," + " use ctrl-C to end");
				
				Client cl = new Client(lvalue,no,1);
				intercl.add(cl);
				Thread g = new Thread(cl);
				cli.add(g);
				g.start();
				
				while(true)
				{
					ClientWorker w;
					try
					{
						w = new ClientWorker(sctpServerChannel.accept(),lvalue,no);
						Thread t = new Thread(w);
						t.start();
					} 
					catch (IOException e) 
					{
						System.out.println("Accept failed");
						System.exit(-1);
					}
				}
			}
			catch (IOException e) 
			{
				System.out.println("Error creating socket");
				System.exit(-1);
			}
		}
	}
	
	public static void main(String[] args)
	{
		AOS_Project1 project = new AOS_Project1();   
	}
}

