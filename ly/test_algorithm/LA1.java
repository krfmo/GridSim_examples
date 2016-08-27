package test_algorithm;

import java.util.Calendar;
import java.util.LinkedList;

import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimRandom;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

/*
 * 关于学习自动机的仿真模拟
 */
public class LA1 extends GridSim{

	public static final int GRIDLET_NUM=10;
	private Integer ID_;//用户ID
	private String name_;//用户名
	private int totalResource_;//资源总数
	private GridletList list_;//任务集合
	private GridletList receiveList_;//从资源传回的任务列表

	private double[] glLengths={5690.0, 10190, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 10606};//随机生成的任务长度

	public LA1(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		this.receiveList_=new GridletList();

		//为用户实体获取ID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("创建一个名为"+name+"，id="+this.ID_+"的网格用户实体");

		//为网格用户创建网格任务列表
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println(name+"：正在创建"+this.list_.size()+"个网格任务");
	}

	@Override
	public void body() {
		int resourceID[]=new int[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];
		ResourceCharacteristics[] resChars=new ResourceCharacteristics[this.totalResource_];

		LinkedList resList;

		while(true){
			super.gridSimHold(1.0);

			resList=super.getGridResourceList();
			if(resList.size()==this.totalResource_){
				break;
			}else{
				System.out.println(this.name_+"：正在等待获取资源列表...");
			}
		}

		for(int i=0; i<this.totalResource_; i++){
			resourceID[i]=((Integer)resList.get(i)).intValue();

			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			resChars[i]=(ResourceCharacteristics) super.receiveEventObject();
			resourceName[i]=resChars[i].getResourceName();

			System.out.println(this.name_+"：已从名为"+resourceName[i]+"，id="+resourceID[i]+"的资源接收到资源属性");

			super.recordStatistics("\"从"+resourceName[i]+"接收到资源属性\"", "");
		}

		////////////
		//接下来要按照调度策略分配任务给对应资源
		schedule(resourceID, resChars);

		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * 基于学习自动机的算法选择部分，根据环境负载状态决定采用何种算法
	 * @param resourceID
	 * @param resChars
	 */
	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars){
		int i;
		int j;
		//step1:生成ect矩阵
		double[][] ect=new double[GRIDLET_NUM][totalResource_];//先生成一个空的矩阵

		//双层循环，得到ECT矩阵，至于任务长度和处理速度相关
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();//任务长度
				int rating=resChars[j].getMIPSRating();//获得资源处理速率
				ect[i][j]=length/rating;
			}
		}

		//资源就绪时间，无论哪种调度策略都需要更新该数组对应下标的值，只不过MCT比较时需要该数组，MET比较时不需要
		double[] ready=new double[totalResource_];
		Gridlet gridlet;
		String info;
		int resID;

		for(i=0; i<GRIDLET_NUM; i++){
			int sch=0;//调度资源的下标
			double[] result;

			//在此调用调度算法，会判断一下，满足一定条件调用MCT算法，否则调用MET算法
			if(i%3==0){
				result=MCT(i, ready, ect);//返回值中包括资源下标sch和下标为sch的资源的就绪时间
			}else if(i%3==1){
				result=MET(i, ect);
			}else{
				result=OBL(i, resChars, ect);
			}
			sch=(int) result[0];
			ready[sch]=result[1];

			//开始提交任务
			gridlet=this.list_.get(i);
			info="网格任务_"+gridlet.getGridletID();
			resID=resourceID[sch];

			System.out.println("发送"+info+"到ID为"+resID+"的资源");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("正在接收网格任务"+gridlet.getGridletID());

			this.receiveList_.add(gridlet);
		}

	}

	private double[] MCT(int i, double[] ready, double[][] ect){
		//int i, j;

		/*//生成ect矩阵
		double[][] ect=new double[GRIDLET_NUM][totalResource_];

		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();
				int rating=resChars[j].getMIPSRating();
				ect[i][j]=length/rating;
			}
		}*/

		/*//计算MCT矩阵
		double[] ready=new double[totalResource_];

		Gridlet gridlet;
		String info;
		int resID;

		for(i=0; i<GRIDLET_NUM; i++){*/
		int sch=0;//调度资源的下标
		double mct=Double.MAX_VALUE;

		//实际上内部循环的内容才是调度算法的真正起作用的内容
		for(int j=0; j<totalResource_; j++){
			double a=ect[i][j]+ready[j];

			if(a<mct){
				mct=a;
				sch=j;
			}
		}

		return new double[]{sch, mct};

		/*ready[sch]=mct;

			//开始提交任务
			gridlet=this.list_.get(i);
			info="网格任务_"+gridlet.getGridletID();
			resID=resourceID[sch];

			System.out.println("发送"+info+"到ID为"+resID+"的资源");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("正在接收网格任务"+gridlet.getGridletID());

			this.receiveList_.add(gridlet);
		}*/
	}

	private double[] MET(int i, double[][] ect){
		/*int i;
		int j;
		//step1:生成ect矩阵
		double[][] ect=new double[GRIDLET_NUM][totalResource_];//先生成一个空的矩阵

		//双层循环，得到ECT矩阵，至于任务长度和处理速度相关
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();//任务长度
				int rating=resChars[j].getMIPSRating();//获得资源处理速率
				ect[i][j]=length/rating;
			}
		}

		Gridlet gridlet;
		String info;
		int resID;//任务将被分配到的资源ID

		for(i=0; i<GRIDLET_NUM; i++){*/
		int sch=0;//调度资源下标
		double met=Double.MAX_VALUE;//一个double值，保存每个任务的最小执行时间
		for(int j=0; j<totalResource_; j++){
			if(ect[i][j]<met){
				met=ect[i][j];
				sch=j;
			}
		}

		return new double[]{sch, met};

		/*//开始提交任务
			gridlet=this.list_.get(i);
			info="网格任务_"+gridlet.getGridletID();
			resID=resourceID[sch];//strategy[i]的内容是第i个任务将被分配到的资源下标

			System.out.println("发送"+info+"到ID为"+resID+"的资源");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("正在接收网格任务"+gridlet.getGridletID());

			this.receiveList_.add(gridlet);
		}*/
	}
	
	private double[] OBL(int i, ResourceCharacteristics[] resChars, double[][] ect){
		int index;
		while(true){
			index=GridSimRandom.intSample(totalResource_);
			if(resChars[index].getNumFreePE()!=0){
				break;
			}
		}
		
		return new double[]{index, ect[i][index]};
	}

	private GridletList getGridletList(){
		return this.receiveList_;
	}

	/**
	 * 为网格用户创建网格任务
	 * @param userID	用户ID
	 * @return	一个任务集合
	 */
	private GridletList createGridlet(int userID){
		GridletList list=new GridletList();

		int id=0;//任务id起始值
		long file_size=300;
		long output_size=300;

		for(int i=0; i<glLengths.length; i++){
			Gridlet gridlet=new Gridlet(id+i, glLengths[i], file_size, output_size);
			gridlet.setUserID(userID);
			list.add(gridlet);
		}

		return list;
	}

	////////////////////////静态方法////////////////////////
	public static void main(String[] args) {
		System.out.println("开始实验！");

		try{
			int num_user=3;//用户数量为3
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;

			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//初始化GridSim包
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);

			//试验过了，操作系统和系统结构对资源没有影响，主要影响资源性能的是资源计算速率MIPSRating
			GridResource resource0=createGridResource("Resource0", 30, "SGI Origin", "Irix");
			GridResource resource1=createGridResource("Resource1", 40, "SGI Origin", "Irix");
			GridResource resource2=createGridResource("Resource2", 50, "SGI Origin", "Irix");
			/*GridResource resource0=createGridResource("Resource0", 30, "Sun Ultra", "Solaris");
			GridResource resource1=createGridResource("Resource1", 40, "Sun Ultra", "Solaris");
			GridResource resource2=createGridResource("Resource2", 50, "Sun Ultra", "Solaris");*/

			int total_resource=3;//上面创建了3个资源，所以这里是3
			LA1 user0=new LA1("User_0", 560.00, total_resource);
			LA1 user1=new LA1("User_1", 560.00, total_resource);
			LA1 user2=new LA1("User_2", 560.00, total_resource);

			GridSim.startGridSimulation();

			GridletList newList=null;
			newList=user0.getGridletList();
			printGridletList(newList, "User_0");
			newList=user1.getGridletList();
			printGridletList(newList, "User_1");
			newList=user2.getGridletList();
			printGridletList(newList, "User_2");
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("出错啦！");
		}
	}

	/**
	 * 创建一个网格资源
	 * @param name	网格资源名
	 * @param mipsRating	计算速率
	 * @param arch	系统结构
	 * @param os	操作系统
	 * @return	网格资源对象
	 */
	private static GridResource createGridResource(String name, int mipsRating, String arch, String os){
		MachineList mList=new MachineList();

		mList.add(new Machine(0, 1, mipsRating));//资源里仅包含一台机器

		double time_zone=0;
		double cost=3.0;

		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.TIME_SHARED, time_zone, cost);

		double baud_rate=100.0;
		long seed=11L*13*17*19*23+1;
		double peakLoad=0.0;
		double offPeakLoad=0.0;
		double holidayLoad=0.0;

		LinkedList<Integer> Weekends=new LinkedList<>();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));

		LinkedList<Integer> Holidays=new LinkedList<>();
		GridResource gridRes=null;
		try {
			gridRes=new GridResource(name, baud_rate, seed,
					resConfig, peakLoad, offPeakLoad, holidayLoad,
					Weekends, Holidays);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("创建一个名为"+name+"的网格资源");
		return gridRes;
	}

	private static void printGridletList(GridletList list, String name) {
		int size=list.size();
		Gridlet gridlet=null;
		double totalET=0;//总处理时长
		double totalCos=0;//总开销
		double totalLen=0;//任务总长度

		String indent="	";
		System.out.println();
		System.out.println("=========="+name+"的输出==========");
		System.out.println("任务ID"+indent+"状态"+indent+indent+"资源ID"+
				indent+"开销"+indent+indent+indent+"任务长度"+
				indent+indent+"处理时长"+indent+indent+indent+"完成时间");

		//打印全部结果的循环
		int i=0;
		double et, cos, len;//处理时长，开销和任务长度
		for(i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			et=gridlet.getActualCPUTime();
			cos=gridlet.getProcessingCost();
			len=gridlet.getGridletLength();
			System.out.print(gridlet.getGridletID()+indent+gridlet.getGridletStatusString());
			if(gridlet.getGridletStatusString().equals("Success")){
				System.out.print(indent);
			}
			System.out.println(indent+gridlet.getResourceID()+indent+cos
					+indent+len+indent+et+indent+gridlet.getFinishTime());
			totalET+=et;
			totalCos+=cos;
			totalLen+=len;
		}

		System.out.println("总处理时长为："+totalET+indent+"总开销为："+totalCos+indent+"任务总长度为："+totalLen);
	}

}






















