package test_algorithm;
/*
 * MCT算法，即最小完成时间算法
 * 将任务以随机的顺序发送至完成时间最小的资源
 * 完成时间包括资源的就绪时间和任务的处理时间
 * 
 * 实验条件：
 * 1.三个资源，每个资源只有一个Machine，每个Machine只有一个PE
 * 2.每个资源的处理速度各不相同，mipsRating分别为37，39，41，第三个资源处理速度最快
 * 3.每个用户都有10个任务，任务长度不完全相同（这样不至于让MCT算法结论与Min-Min算法结论相同）
 */
import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

import java.util.Calendar;
import java.util.LinkedList;

public class TestMCT extends GridSim{
	public static final int GRIDLET_NUM=10;//需要创建的任务总数
	//private static List<GridResource> resourceList=new ArrayList<>();//资源总数是静态的，所有用户共享这些资源

	private Integer ID_;//用户ID
	private String name_;//用户名
	private int totalResource_;//资源总数（本例为3）
	private GridletList list_;//任务集合
	private GridletList receiveList_;//从资源传回的任务列表，此时任务状态已改

	private double[] glLengths={5690.0, 10190, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 10606};//随机生成的任务长度，总数与常量GRIDLET_NUM对应
	/*private double[] glLengths={569.0, 1019.0, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 1060.6};*///随机生成的任务长度，总数与常量GRIDLET_NUM对应

	public TestMCT(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		this.receiveList_=new GridletList();

		//为用户实体获取ID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("创建一个名为"+name+"，id="+this.ID_ +"的网格用户实体");

		//为网格用户创建网格任务列表
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println(name+":正在创建"+this.list_.size()+"个网格任务");
	}

	@Override
	public void body() {
		int resourceID[]=new int[this.totalResource_];
		//double resourceCost[]=new double[this.totalResource_];	//暂时没必要
		String resourceName[]=new String[this.totalResource_];
		//int[] strategy=schedule();	//调度策略算法先不再这里获取，当获取到所有资源后再调度任务
		ResourceCharacteristics[] resChars=new ResourceCharacteristics[this.totalResource_];//装的是资源属性对象

		LinkedList resList;
		//ResourceCharacteristics resChar;

		/*
		 * 等待获取资源列表。GridSim包采用多线程环境，所以你的请求可能比网格资源注册
		 * 到GIS实体到达的早。因此，必须先等待。
		 */
		while(true){
			//需要暂停一下等待网格资源结束向GIS的注册
			super.gridSimHold(1.0);//持续1s

			resList=super.getGridResourceList();//GridSim里的方法
			if(resList.size()==this.totalResource_){
				break;
			}else{
				System.out.println(this.name_+"：正在等待获取资源列表...");
			}
		}

		//一个循环，来得到所有可用资源
		int i, j;
		for(i=0; i<this.totalResource_; i++){
			//资源列表包含的是资源ID而不是资源对象
			resourceID[i]=((Integer)resList.get(i)).intValue();

			//向资源实体发送其属性的请求
			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW,
					GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			//等待获取一个资源属性
			resChars[i]=(ResourceCharacteristics) super.receiveEventObject();
			resourceName[i]=resChars[i].getResourceName();
			//resourceCost[i]=resChar.getCostPerSec();

			System.out.println(this.name_+"：已从名为"+resourceName[i]+"，id="+resourceID[i]+"的资源接收到资源属性");

			//将事件记录在"stat.txt"文件
			super.recordStatistics("\"从"+resourceName[i]+"接收到资源属性\"", "");
		}

		////////////////////////////
		//接下来要按照生成的调度策略分配任务给对应资源
		schedule(resourceID, resChars);

		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars) {
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

		//step2:计算MCT矩阵
		double[] ready=new double[totalResource_];//存放每个资源就绪时间的double数组，每当分配一个任务，对应的资源就绪时间都要调整

		Gridlet gridlet;
		String info;
		int resID;//任务将被分配到的资源ID

		for(i=0; i<GRIDLET_NUM; i++){
			int sch=0;//调度资源的下标
			double mct=Double.MAX_VALUE;

			/*
			 * 外层循环要做的事：
			 * 1.更新第i个任务在每个资源上的完成时间
			 * 	 即，ect[i][j]+ready[j]
			 * 2.比较第i个任务在每个资源上的完成时间（即更新后的ect[i][0]到ect[i][numRes]）
			 * 3.得到上一步比较的最小值对应的下标值（对应的j），保存到一个长为GRIDLET_NUM的int数组中
			 * 4.跳出内层循环，把更新对应的资源就需时间为新的完成时间
			 * 5.i值自增，开始下一轮外层循环
			 */
			for(j=0; j<totalResource_; j++){
				double a=ect[i][j]+ready[j];//更新为任务完成时间

				//若在这台机器上任务的完成时间更短，则更新最短完成时间和分配策略
				if(a<mct){
					mct=a;
					sch=j;
				}
			}
			//更新ready[任务分配到的资源下标]为资源新的就绪时间，即mct[i]
			ready[sch]=mct;

			//开始提交任务
			gridlet=this.list_.get(i);
			info="网格任务_"+gridlet.getGridletID();
			resID=resourceID[sch];//sch是第i个任务将被分配到的资源下标

			System.out.println("发送"+info+"到ID为"+resID+"的资源");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("正在接收网格任务"+gridlet.getGridletID());

			this.receiveList_.add(gridlet);
		}
	}


	private GridletList getGridletList() {
		return this.receiveList_;
	}


	/**
	 * 为网格用户创建GRIDLET_NUM个网格任务，返回一个任务集合
	 * 采用固定参数创建任务，所以每个用户的任务长度是相同的
	 * @param userID	用户ID
	 * @return 一个任务集合
	 */
	private GridletList createGridlet(int userID) {
		GridletList list=new GridletList();

		int id=0;//任务id起始值
		//double length=268845.0;//任务长度
		long file_size=300;
		long output_size=300;

		for(int i=0; i<GRIDLET_NUM;i++){
			//list.add(new Gridlet(id+i, length*i, file_size, output_size));
			Gridlet gridlet=new Gridlet(id+i, glLengths[i], file_size, output_size);
			gridlet.setUserID(userID);//差点忘了这一步...
			list.add(gridlet);
		}

		/*//设置PE的MIPS率
		GridSimStandardPE.setRating(100);

		//循环，随机生成GRIDLET_NUM个任务
		for(int i=0; i<GRIDLET_NUM; i++){
			length=GridSimStandardPE.toMIs(GridSimRandom.doubleSample()*500);
			file_size=(long) GridSimRandom.real(100, 0.10, 0.40, GridSimRandom.doubleSample());
			output_size=(long) GridSimRandom.real(250, 0.10, 0.50, GridSimRandom.doubleSample());

			Gridlet gridlet=new Gridlet(id+i, length, file_size, output_size);

			gridlet.setUserID(userID);
			list.add(gridlet);
		}*/

		return list;//险些忘记改了，返回null...
	}

	////////////////////////静态方法///////////////////////

	public static void main(String[] args) {
		System.out.println("开始实验！");

		try {
			//int num_user=1;//只有一个用户
			int num_user=3;//试一下3个用户
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;

			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//初始化GridSim包。
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, 
					exclude_from_processing, report_name);

			/*GridResource resource0=createGridResource("Resource_0", 37);
			GridResource resource1=createGridResource("Resource_1", 39);
			GridResource resource2=createGridResource("Resource_2", 41);*/
			GridResource resource0=createGridResource("Resource_0", 30);
			GridResource resource1=createGridResource("Resource_1", 40);
			GridResource resource2=createGridResource("Resource_2", 50);
			//resourceList.add(resource0);
			//resourceList.add(resource1);
			//resourceList.add(resource2);

			int total_resource=3;//与上面创建资源的次数相对应

			TestMCT user0=new TestMCT("User_0", 560.00, total_resource);//创建网格用户，同时生成了任务集合
			TestMCT user1=new TestMCT("User_1", 560.00, total_resource);//创建网格用户，同时生成了任务集合
			TestMCT user2=new TestMCT("User_2", 560.00, total_resource);//创建网格用户，同时生成了任务集合
			//接下来就可以根据任务和资源信息生成调度策略了，不过貌似策略应该在body里调用才有意义

			GridSim.startGridSimulation();

			GridletList newList=null;
			newList=user0.getGridletList();
			printGridletList(newList, "User_0");
			newList=user1.getGridletList();
			printGridletList(newList, "User_1");
			newList=user2.getGridletList();
			printGridletList(newList, "User_2");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("出错啦！");
		}
	}

	/**
	 * 创建一个网格资源。
	 * @param name	一个网格资源名
	 * @return	一个网格资源对象
	 */
	private static GridResource createGridResource(String name, int mipsRating){
		//1.创建一个机器列表对象来存储一台或多台机器
		MachineList mList=new MachineList();

		//2.创建机器
		//int mipsRating=377;
		mList.add(new Machine(0, 1, mipsRating));//只创建一台机器，只有一个PE

		/*//3.若需创建更多机器则重复步骤2
		mList.add(new Machine(1, 4, mipsRating));//第二台机器
		mList.add(new Machine(2, 2, mipsRating));//第三台机器*/

		//4.创建一个资源属性对象，来存储网格资源属性
		String arch="Sun Ultra";
		String os="Solaris";
		double time_zone=9.0;
		double cost=3.0;

		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.TIME_SHARED,
				time_zone, cost);

		//5.最后，创建网格资源对象
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
					resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends,
					Holidays);
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

