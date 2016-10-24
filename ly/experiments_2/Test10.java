package experiments_2;

import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimRandom;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

/*
 * 仿真实验10
 * 若处理能力最强的资源的就绪时间最短，则将任务分配给该资源处理，否则采用OBL算法分配任务
 * 实验条件：
 * 用户数	3
 * 任务数	100
 * 任务长度	10000(高斯分布)
 * 长短任务比例	0(全部是长任务)(可以从结果知道，长任务比例越大，MCT结果越好，长任务比例越小，OBL结果越好)
 * 资源数	3
 * 资源处理能力差异	30，40，50
 */
public class Test10 extends GridSim{

	//各种常量
	//用户数（更改此值需保证main方法中创建的用户数与此数值一致）
	public static final int USER_NUM=3;
	//与一个用户关联的任务数
	public static final int GRIDLET_NUM=100;
	//生成长度服从高斯分布的任务时用到的均值和方差的参数（任务长度）
	public static final int GAUSSIAN_CONST_1=1000;//短任务
	public static final int GAUSSIAN_CONST_2=10000;//长任务
	//长短任务比例，0的短任务，1的长任务吧
	public static final float PER_CENTAGE=0f;
	//资源总数（更改此值需保证main方法中创建的用户数与此数值一致）
	public static final int RESOURCE_NUM=3;
	//资源处理能力（有多少个资源就应该有多少mips_rating值）
	public static final int MIPS_RATING_1=30;
	public static final int MIPS_RATING_2=40;
	public static final int MIPS_RATING_3=50;

	//成员变量
	private Integer ID_;//用户ID
	private String name_;//用户名
	private int totalResource_;//资源总数
	private GridletList list_;//任务集合
	private GridletList receiveList_;//从资源传回的任务列表

	//任务长度数组，任务为服从高斯分布的int值，任务数为常量GRIDLET_NUM
	//这里只能保证运行一次各个用户都有相同的任务集合，但是不同次运行的任务集合是不同的，以后有必要可以把一组任务长度数据保存，然后每次用同一组数据
	//private int[] glLengths=createGL();
	private int[] glLengths={9971, 10035, 10109, 9997, 10021, 9950, 10017, 10029, 10111, 9908, 10063,
			10008, 9929, 9983, 9888, 9955, 10224, 10206, 10144, 9916, 9900, 9989, 10066, 10107, 9932,
			9958, 10190, 10062, 9940, 9994, 9877, 9921, 10187, 9939, 10036, 9989, 9758, 9888, 10051,
			9913, 9960, 9755, 9982, 10063, 10130, 9953, 9979, 10105, 9976, 9874, 9911, 10053, 10129,
			9781, 9952, 9910, 9972, 10034, 9959, 10018, 10129, 10114, 9945, 10008, 10038, 9967, 9930,
			9944, 9910, 10139, 10113, 10071, 9958, 10025, 10131, 10083, 9837, 10148, 9932, 9913, 10088,
			9820, 9888, 10041, 9984, 9960, 10109, 9909, 9878, 9918, 10076, 9891, 9952, 9918, 10139, 10032,
			10041, 10139, 9917, 10119};

	public Test10(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		//先不改动，在创建用户时就创建接收处理后返回的任务队列
		this.receiveList_=new GridletList();

		//为用户实体获取ID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("创建一个名为"+name+"，id="+this.ID_+"的网格用户实体");

		//为网格用户创建网格任务列表
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println(name+"：正在创建"+this.list_.size()+"个网格任务");
	}

	/**
	 * 生成任务数组的方法
	 * 生成GRIDLET_NUM个服从高斯分布的任务
	 * 
	 * @return 返回值为一个int数组，数组长度为GRIDLET_NUM
	 */
	private int[] createGL() {
		Random random=new Random();
		int shortNum=(int)(PER_CENTAGE*GRIDLET_NUM);
		int longNum=GRIDLET_NUM-shortNum;
		System.out.println("短任务数："+shortNum+"，长任务数："+longNum);
		int[] index = generateRandomIndex();
		
		int[] glls=new int[GRIDLET_NUM];
		for(int i=0; i<shortNum; i++){
			glls[index[i]]=(int) (Math.sqrt(GAUSSIAN_CONST_1)*random.nextGaussian()+GAUSSIAN_CONST_1);
		}
		for(int i=0; i<longNum; i++){
			glls[index[i+shortNum]]=(int) (Math.sqrt(GAUSSIAN_CONST_2)*random.nextGaussian()+GAUSSIAN_CONST_2);
		}
		
		System.out.println(Arrays.toString(glls));
		
		return glls;
	}

	/**
	 * 该方法生成随机的下标值，这样可以打乱长短任务的顺序
	 * @return	元素为0-99的随机值的int数组
	 */
	private int[] generateRandomIndex() {
		//该数组存放的是下标值，随机的0-99
		int[] index=new int[GRIDLET_NUM];
		
		//初始值为false，一旦确定将某0-99的随机整数放入index数组，则将flag数组中下标为该整数的元素置为true，表示该下标已被分配
		boolean[] flag=new boolean[GRIDLET_NUM];
		for(int i=0; i<GRIDLET_NUM; i++){
			int j;
			do{
				j=new Random().nextInt(GRIDLET_NUM);
			}while(flag[j]);
			
			index[i]=j;
			flag[j]=true;
		}
		return index;
	}

	@Override
	public void body() {
		int resourceID[]=new int[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];
		ResourceCharacteristics[] resChars=new ResourceCharacteristics[this.totalResource_];

		LinkedList<Integer> resList;

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
			resourceID[i]=resList.get(i).intValue();

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
	 * @throws Exception 
	 */
	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars) {
		int i;
		int j;
		//step1:生成etc矩阵
		double[][] etc=new double[GRIDLET_NUM][totalResource_];//先生成一个空的矩阵

		//双层循环，得到ECT矩阵，至于任务长度和处理速度相关
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				//double length=queue_.take().getGridletLength();//任务长度
				//算了，直接用数组来计算ETC矩阵吧，静态的容易些
				int length=glLengths[i];
				int rating=resChars[j].getMIPSRating();//获得资源处理速率
				etc[i][j]=length/rating;
			}
		}

		//资源就绪时间，无论哪种调度策略都需要更新该数组对应下标的值，只不过MCT比较时需要该数组，MET比较时不需要
		double[] ready=new double[totalResource_];
		Gridlet gridlet;
		//String info;
		//int resID;

		//step2:循环调度每个任务，并且根据具体情况制定下个任务调度该使用的调度算法
		for(i=0; i<GRIDLET_NUM; i++){
			int sch=0;//调度资源的下标
			double minTime;//处理每个任务的最短处理时间
			double[] result;//返回值依次为{处理任务的资源的下标，任务处理完成后预计的完成时间，理论上该任务最短的处理时长}

			////////////////////////新增内容////////////////////////

			if((ready[2]<ready[0])&&(ready[2]<ready[1])){
				result=MET(i, ready, etc);
			}else{

				////////////////////////新增内容////////////////////////

				//在此调用调度算法，会判断一下，满足一定条件调用MCT算法，否则调用MET算法
				/*if(nextP<=0.78){
					result=MCT(i, ready, etc);//返回值中包括资源下标sch和下标为sch的资源的就绪时间
				}else if(nextP<=0.8){
					result=OBL(i, ready, resChars, etc);
				}else{
					result=MET(i, ready, etc);
				}*/

				//单独测试结果：MCT>OBL>MET，MCT与OBL结合结果更优
				//result=MCT(i, ready, etc);
				//result=MET(i, ready, etc);
				result=OBL(i, ready, resChars, etc);
			}

			//获取调度算法结果，结果数组中第一个元素为目标机器下标，第二个元素为目标机器完成时间，第三个元素为理论上处理任务的最短时长
			sch=(int) result[0];
			ready[sch]=result[1];
			minTime=(double) result[2];

			//开始提交任务
			gridlet=this.list_.get(i);
			super.gridletSubmit(gridlet, resourceID[sch]);

			//接收返回的已处理任务，并将其添加到已处理任务列表
			gridlet=super.gridletReceive();
			this.receiveList_.add(gridlet);
			
			//按照作者给出的公式，计算各个描述系统特征的参数，然后确定下次调度使用的算法
			//TODO 返回值形式暂时没确定......
			nextAlgorithm(ready, minTime);
		}

	}

	private double totalMinTime;//用于存储总的理论处理最短时长和
	public static final double b=0.5;//惩罚值，这里设置a=b，即奖励值与惩罚值相等
	private static double nextP=0;//静态成员，记录每次分配任务后，根据环境响应的变化，决定下次分配应采用的调度算法，设置其初始值为1
	private void nextAlgorithm(double[] ready, double minTime) {
		totalMinTime+=minTime;
		//匹配近似度
		double mp=computeYita(totalMinTime, ready);
		//系统利用率
		double su=computeDelta(ready);
		
		//System.out.println("匹配近似度为："+mp+"系统利用率为："+su);
		
		//TODO 根据匹配近似度和系统利用率的值，确定下次分配的调度算法
		double beta=mp+su;//环境响应，学习自动机的输入
		nextP=(1-b)*nextP+b*b*beta;
		System.out.println("环境响应为："+nextP);
	}

	/**
	 * 计算匹配近似度的方法
	 * @param totalMinTime2
	 * @param ready
	 * @return	返回匹配近似度
	 */
	private double computeYita(double totalMinTime2, double[] ready) {
		double totalTime=0;//完成所有已分配任务所需总时间
		for(int i=0; i<ready.length; i++){
			totalTime+=ready[i];
		}
		
		double matchingProximity=totalMinTime2/totalTime;
		
		return matchingProximity;
	}

	/**
	 * 计算系统利用率的方法
	 * @param ready	
	 * @return	返回系统利用率
	 */
	private double computeDelta(double[] ready) {
		double totalTime=0;//完成所有已分配任务所需总时间
		double maxTime=0;//记录最大的ready元素，即时间跨度，makespan
		for(int i=0; i<ready.length; i++){
			if(ready[i]>maxTime){
				maxTime=ready[i];
			}
			
			totalTime+=ready[i];
		}
		
		double sysUtilization=totalTime/(maxTime*totalResource_);
		
		return sysUtilization;
	}

	private double[] MCT(int i, double[] ready, double[][] etc){
		int sch=0;//调度资源的下标
		double minTime=Double.MAX_VALUE;//记录etc矩阵中最短处理时长的时间（只记录时间即可，不需记录对应资源下标）
		double mct=Double.MAX_VALUE;

		//实际上内部循环的内容才是调度算法的真正起作用的内容
		for(int j=0; j<totalResource_; j++){
			double a=etc[i][j]+ready[j];
			
			//为了找到完成时间最小的资源
			if(a<mct){
				mct=a;
				sch=j;
			}
			
			//为了找到处理时间最小的资源
			if(etc[i][j]<minTime){
				minTime=etc[i][j];
			}
		}

		return new double[]{sch, mct, minTime};
	}

	private double[] MET(int i, double[] ready, double[][] etc){
		int sch=0;//调度资源下标
		double met=Double.MAX_VALUE;//一个double值，保存每个任务的最小执行时间
		for(int j=0; j<totalResource_; j++){
			if(etc[i][j]<met){
				met=etc[i][j];
				sch=j;
			}
		}

		return new double[]{sch, met+ready[sch], met};

	}

	private double[] OBL(int i, double[] ready, ResourceCharacteristics[] resChars, double[][] etc){
		int index;
		double minTime=Double.MAX_VALUE;
		while(true){
			index=GridSimRandom.intSample(totalResource_);
			if(resChars[index].getNumFreePE()!=0){
				break;
			}
		}
		
		for(int j=0; j<totalResource_; j++){
			if(etc[i][j]<minTime){
				minTime=etc[i][j];
			}
		}

		return new double[]{index, etc[i][index]+ready[index], minTime};
	}

	private GridletList getGridletList(){
		return this.receiveList_;
	}

	/**
	 * 为网格用户创建网格任务
	 * @param userID	用户ID
	 * @return	一个任务队列
	 */
	private GridletList createGridlet(int userID){
		final GridletList list=new GridletList();

		int id=0;//任务id起始值
		long file_size=300;
		long output_size=300;

		for(int i=0; i<glLengths.length; i++){
			final Gridlet gridlet=new Gridlet(id+i, glLengths[i], file_size, output_size);
			gridlet.setUserID(userID);
			/*//设置定时器，希望每10ms添加一个任务
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						queue.put(gridlet);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 10);*/

			list.add(gridlet);
		}

		return list;
	}

	////////////////////////静态方法////////////////////////
	public static void main(String[] args) {
		System.out.println("开始实验！");

		try{
			int num_user=USER_NUM;//用户数量为3
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;

			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//初始化GridSim包
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);

			//试验过了，操作系统和系统结构对资源没有影响，主要影响资源性能的是资源计算速率MIPSRating
			/*GridResource resource0=createGridResource("Resource0", 37, "SGI Origin", "Irix");
			GridResource resource1=createGridResource("Resource1", 39, "SGI Origin", "Irix");
			GridResource resource2=createGridResource("Resource2", 41, "SGI Origin", "Irix");*/
			/*GridResource resource0=createGridResource("Resource0", 30, "SGI Origin", "Irix");
			GridResource resource1=createGridResource("Resource1", 40, "SGI Origin", "Irix");
			GridResource resource2=createGridResource("Resource2", 50, "SGI Origin", "Irix");*/
			GridResource resource0=createGridResource("Resource0", MIPS_RATING_1, "Sun Ultra", "Solaris");
			GridResource resource1=createGridResource("Resource1", MIPS_RATING_2, "Sun Ultra", "Solaris");
			GridResource resource2=createGridResource("Resource2", MIPS_RATING_3, "Sun Ultra", "Solaris");

			int total_resource=RESOURCE_NUM;//上面创建了3个资源，所以这里是3
			Test10 user0=new Test10("User_0", 560.00, total_resource);
			Test10 user1=new Test10("User_1", 560.00, total_resource);
			Test10 user2=new Test10("User_2", 560.00, total_resource);

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
		/*System.out.println("任务ID"+indent+"状态"+indent+indent+"资源ID"+
				indent+"开销"+indent+indent+indent+"任务长度"+
				indent+indent+"处理时长"+indent+indent+indent+"完成时间");*/

		//打印全部结果的循环
		int i=0;
		double et, cos, len;//处理时长，开销和任务长度
		for(i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			et=gridlet.getActualCPUTime();
			cos=gridlet.getProcessingCost();
			len=gridlet.getGridletLength();
			/*System.out.print(gridlet.getGridletID()+indent+gridlet.getGridletStatusString());
			if(gridlet.getGridletStatusString().equals("Success")){
				System.out.print(indent);
			}
			System.out.println(indent+gridlet.getResourceID()+indent+cos
					+indent+len+indent+et+indent+gridlet.getFinishTime());*/
			if(i==GRIDLET_NUM-1){
				System.out.println("完成时间为："+gridlet.getFinishTime());
			}
			totalET+=et;
			totalCos+=cos;
			totalLen+=len;
		}

		System.out.println("总处理时长为："+totalET+indent+"总开销为："+totalCos+indent+"任务总长度为："+totalLen);
	}

}






















