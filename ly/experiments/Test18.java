package experiments;

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
 * ����ʵ��18
 * ������ֳ�����ģ�ⲻͬʵ�������¸����㷨�ĵ��Ƚ��
 * ʵ��������
 * �û���	3
 * ������	100
 * ���񳤶�	10000����˹�ֲ���
 * �����������	0.6
 * ��Դ��	3
 * ��Դ������������	37��39��41
 */
public class Test18 extends GridSim{

	//���ֳ���
	//�û��������Ĵ�ֵ�豣֤main�����д������û��������ֵһ�£�
	public static final int USER_NUM=3;
	//��һ���û�������������
	public static final int GRIDLET_NUM=100;
	//���ɳ��ȷ��Ӹ�˹�ֲ�������ʱ�õ��ľ�ֵ�ͷ���Ĳ��������񳤶ȣ�
	public static final int GAUSSIAN_CONST_1=1000;//������
	public static final int GAUSSIAN_CONST_2=10000;//������
	//�������������0.6�Ķ�����0.4�ĳ������
	public static final float PER_CENTAGE=0.6f;
	//��Դ���������Ĵ�ֵ�豣֤main�����д������û��������ֵһ�£�
	public static final int RESOURCE_NUM=3;
	//��Դ�����������ж��ٸ���Դ��Ӧ���ж���mips_ratingֵ��
	public static final int MIPS_RATING_1=37;
	public static final int MIPS_RATING_2=39;
	public static final int MIPS_RATING_3=41;

	//��Ա����
	private Integer ID_;//�û�ID
	private String name_;//�û���
	private int totalResource_;//��Դ����
	private GridletList list_;//���񼯺�
	private GridletList receiveList_;//����Դ���ص������б�

	//���񳤶����飬����Ϊ���Ӹ�˹�ֲ���intֵ��������Ϊ����GRIDLET_NUM
	//����ֻ�ܱ�֤����һ�θ����û�������ͬ�����񼯺ϣ����ǲ�ͬ�����е����񼯺��ǲ�ͬ�ģ��Ժ��б�Ҫ���԰�һ�����񳤶����ݱ��棬Ȼ��ÿ����ͬһ������
	//private int[] glLengths=createGL();
	private int[] glLengths={1065, 10065, 958, 1011, 1038, 9863, 9966, 1010, 9952, 1006, 985, 1013,
			1005, 997, 10053, 1005, 994, 963, 977, 1023, 1028, 10064, 966, 9939, 9923, 10063, 995,
			10148, 10087, 10093, 988, 10022, 9941, 949, 10042, 10106, 1025, 1063, 10025, 10011, 1015,
			1022, 10009, 1017, 9953, 990, 10012, 1013, 938, 10010, 999, 9902, 10081, 999, 1021, 1040,
			979, 10096, 9830, 1002, 9968, 1025, 981, 9898, 10007, 994, 1091, 987, 970, 1022, 10171,
			1010, 1038, 10008, 10013, 1028, 10041, 1031, 9945, 10144, 1011, 1012, 1036, 10073, 995,
			1014, 9948, 987, 996, 957, 977, 927, 1009, 977, 9880, 1008, 1024, 9968, 1017, 9924};

	public Test18(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		//�Ȳ��Ķ����ڴ����û�ʱ�ʹ������մ���󷵻ص��������
		this.receiveList_=new GridletList();

		//Ϊ�û�ʵ���ȡID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("����һ����Ϊ"+name+"��id="+this.ID_+"�������û�ʵ��");

		//Ϊ�����û��������������б�
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println(name+"�����ڴ���"+this.list_.size()+"����������");
	}

	/**
	 * ������������ķ���
	 * ����GRIDLET_NUM�����Ӹ�˹�ֲ�������
	 * 
	 * @return ����ֵΪһ��int���飬���鳤��ΪGRIDLET_NUM
	 */
	private int[] createGL() {
		Random random=new Random();
		int shortNum=(int)(PER_CENTAGE*GRIDLET_NUM);
		int longNum=GRIDLET_NUM-shortNum;
		System.out.println("����������"+shortNum+"������������"+longNum);
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
	 * �÷�������������±�ֵ���������Դ��ҳ��������˳��
	 * @return	Ԫ��Ϊ0-99�����ֵ��int����
	 */
	private int[] generateRandomIndex() {
		//�������ŵ����±�ֵ�������0-99
		int[] index=new int[GRIDLET_NUM];
		
		//��ʼֵΪfalse��һ��ȷ����ĳ0-99�������������index���飬��flag�������±�Ϊ��������Ԫ����Ϊtrue����ʾ���±��ѱ�����
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
				System.out.println(this.name_+"�����ڵȴ���ȡ��Դ�б�...");
			}
		}

		for(int i=0; i<this.totalResource_; i++){
			resourceID[i]=resList.get(i).intValue();

			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			resChars[i]=(ResourceCharacteristics) super.receiveEventObject();
			resourceName[i]=resChars[i].getResourceName();

			System.out.println(this.name_+"���Ѵ���Ϊ"+resourceName[i]+"��id="+resourceID[i]+"����Դ���յ���Դ����");

			super.recordStatistics("\"��"+resourceName[i]+"���յ���Դ����\"", "");
		}

		////////////
		//������Ҫ���յ��Ȳ��Է����������Ӧ��Դ
		schedule(resourceID, resChars);

		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * ����ѧϰ�Զ������㷨ѡ�񲿷֣����ݻ�������״̬�������ú����㷨
	 * @param resourceID
	 * @param resChars
	 * @throws Exception 
	 */
	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars) {
		int i;
		int j;
		//step1:����etc����
		double[][] etc=new double[GRIDLET_NUM][totalResource_];//������һ���յľ���

		//˫��ѭ�����õ�ECT�����������񳤶Ⱥʹ����ٶ����
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				//double length=queue_.take().getGridletLength();//���񳤶�
				//���ˣ�ֱ��������������ETC����ɣ���̬������Щ
				int length=glLengths[i];
				int rating=resChars[j].getMIPSRating();//�����Դ��������
				etc[i][j]=length/rating;
			}
		}

		//��Դ����ʱ�䣬�������ֵ��Ȳ��Զ���Ҫ���¸������Ӧ�±��ֵ��ֻ����MCT�Ƚ�ʱ��Ҫ�����飬MET�Ƚ�ʱ����Ҫ
		double[] ready=new double[totalResource_];
		Gridlet gridlet;
		//String info;
		//int resID;

		//step2:ѭ������ÿ�����񣬲��Ҹ��ݾ�������ƶ��¸�������ȸ�ʹ�õĵ����㷨
		for(i=0; i<GRIDLET_NUM; i++){
			int sch=0;//������Դ���±�
			double minTime;//����ÿ���������̴���ʱ��
			double[] result;//����ֵ����Ϊ{�����������Դ���±꣬��������ɺ�Ԥ�Ƶ����ʱ�䣬�����ϸ�������̵Ĵ���ʱ��}

			//�ڴ˵��õ����㷨�����ж�һ�£�����һ����������MCT�㷨���������MET�㷨
			/*if(nextP<=0.78){
				result=MCT(i, ready, etc);//����ֵ�а�����Դ�±�sch���±�Ϊsch����Դ�ľ���ʱ��
			}else if(nextP<=0.8){
				result=OBL(i, ready, resChars, etc);
			}else{
				result=MET(i, ready, etc);
			}*/

			//�������Խ����MCT>OBL>MET��MCT��OBL��Ͻ������
			//result=MCT(i, ready, etc);
			//result=MET(i, ready, etc);
			result=OBL(i, ready, resChars, etc);

			//��ȡ�����㷨�������������е�һ��Ԫ��ΪĿ������±꣬�ڶ���Ԫ��ΪĿ��������ʱ�䣬������Ԫ��Ϊ�����ϴ�����������ʱ��
			sch=(int) result[0];
			ready[sch]=result[1];
			minTime=(double) result[2];

			//��ʼ�ύ����
			gridlet=this.list_.get(i);
			super.gridletSubmit(gridlet, resourceID[sch]);

			//���շ��ص��Ѵ������񣬲�������ӵ��Ѵ��������б�
			gridlet=super.gridletReceive();
			this.receiveList_.add(gridlet);
			
			//�������߸����Ĺ�ʽ�������������ϵͳ�����Ĳ�����Ȼ��ȷ���´ε���ʹ�õ��㷨
			//TODO ����ֵ��ʽ��ʱûȷ��......
			nextAlgorithm(ready, minTime);
		}

	}

	private double totalMinTime;//���ڴ洢�ܵ����۴������ʱ����
	public static final double b=0.5;//�ͷ�ֵ����������a=b��������ֵ��ͷ�ֵ���
	private static double nextP=0;//��̬��Ա����¼ÿ�η�������󣬸��ݻ�����Ӧ�ı仯�������´η���Ӧ���õĵ����㷨���������ʼֵΪ1
	private void nextAlgorithm(double[] ready, double minTime) {
		totalMinTime+=minTime;
		//ƥ����ƶ�
		double mp=computeYita(totalMinTime, ready);
		//ϵͳ������
		double su=computeDelta(ready);
		
		//System.out.println("ƥ����ƶ�Ϊ��"+mp+"ϵͳ������Ϊ��"+su);
		
		//TODO ����ƥ����ƶȺ�ϵͳ�����ʵ�ֵ��ȷ���´η���ĵ����㷨
		double beta=mp+su;//������Ӧ��ѧϰ�Զ���������
		nextP=(1-b)*nextP+b*b*beta;
		System.out.println("������ӦΪ��"+nextP);
	}

	/**
	 * ����ƥ����ƶȵķ���
	 * @param totalMinTime2
	 * @param ready
	 * @return	����ƥ����ƶ�
	 */
	private double computeYita(double totalMinTime2, double[] ready) {
		double totalTime=0;//��������ѷ�������������ʱ��
		for(int i=0; i<ready.length; i++){
			totalTime+=ready[i];
		}
		
		double matchingProximity=totalMinTime2/totalTime;
		
		return matchingProximity;
	}

	/**
	 * ����ϵͳ�����ʵķ���
	 * @param ready	
	 * @return	����ϵͳ������
	 */
	private double computeDelta(double[] ready) {
		double totalTime=0;//��������ѷ�������������ʱ��
		double maxTime=0;//��¼����readyԪ�أ���ʱ���ȣ�makespan
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
		int sch=0;//������Դ���±�
		double minTime=Double.MAX_VALUE;//��¼etc��������̴���ʱ����ʱ�䣨ֻ��¼ʱ�伴�ɣ������¼��Ӧ��Դ�±꣩
		double mct=Double.MAX_VALUE;

		//ʵ�����ڲ�ѭ�������ݲ��ǵ����㷨�����������õ�����
		for(int j=0; j<totalResource_; j++){
			double a=etc[i][j]+ready[j];
			
			//Ϊ���ҵ����ʱ����С����Դ
			if(a<mct){
				mct=a;
				sch=j;
			}
			
			//Ϊ���ҵ�����ʱ����С����Դ
			if(etc[i][j]<minTime){
				minTime=etc[i][j];
			}
		}

		return new double[]{sch, mct, minTime};
	}

	private double[] MET(int i, double[] ready, double[][] etc){
		int sch=0;//������Դ�±�
		double met=Double.MAX_VALUE;//һ��doubleֵ������ÿ���������Сִ��ʱ��
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
	 * Ϊ�����û�������������
	 * @param userID	�û�ID
	 * @return	һ���������
	 */
	private GridletList createGridlet(int userID){
		final GridletList list=new GridletList();

		int id=0;//����id��ʼֵ
		long file_size=300;
		long output_size=300;

		for(int i=0; i<glLengths.length; i++){
			final Gridlet gridlet=new Gridlet(id+i, glLengths[i], file_size, output_size);
			gridlet.setUserID(userID);
			/*//���ö�ʱ����ϣ��ÿ10ms���һ������
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

	////////////////////////��̬����////////////////////////
	public static void main(String[] args) {
		System.out.println("��ʼʵ�飡");

		try{
			int num_user=USER_NUM;//�û�����Ϊ3
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;

			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//��ʼ��GridSim��
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);

			//������ˣ�����ϵͳ��ϵͳ�ṹ����Դû��Ӱ�죬��ҪӰ����Դ���ܵ�����Դ��������MIPSRating
			/*GridResource resource0=createGridResource("Resource0", 37, "SGI Origin", "Irix");
			GridResource resource1=createGridResource("Resource1", 39, "SGI Origin", "Irix");
			GridResource resource2=createGridResource("Resource2", 41, "SGI Origin", "Irix");*/
			/*GridResource resource0=createGridResource("Resource0", 30, "SGI Origin", "Irix");
			GridResource resource1=createGridResource("Resource1", 40, "SGI Origin", "Irix");
			GridResource resource2=createGridResource("Resource2", 50, "SGI Origin", "Irix");*/
			GridResource resource0=createGridResource("Resource0", MIPS_RATING_1, "Sun Ultra", "Solaris");
			GridResource resource1=createGridResource("Resource1", MIPS_RATING_2, "Sun Ultra", "Solaris");
			GridResource resource2=createGridResource("Resource2", MIPS_RATING_3, "Sun Ultra", "Solaris");

			int total_resource=RESOURCE_NUM;//���洴����3����Դ������������3
			Test18 user0=new Test18("User_0", 560.00, total_resource);
			Test18 user1=new Test18("User_1", 560.00, total_resource);
			Test18 user2=new Test18("User_2", 560.00, total_resource);

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
			System.out.println("��������");
		}
	}

	/**
	 * ����һ��������Դ
	 * @param name	������Դ��
	 * @param mipsRating	��������
	 * @param arch	ϵͳ�ṹ
	 * @param os	����ϵͳ
	 * @return	������Դ����
	 */
	private static GridResource createGridResource(String name, int mipsRating, String arch, String os){
		MachineList mList=new MachineList();

		mList.add(new Machine(0, 1, mipsRating));//��Դ�������һ̨����

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

		System.out.println("����һ����Ϊ"+name+"��������Դ");
		return gridRes;
	}

	private static void printGridletList(GridletList list, String name) {
		int size=list.size();
		Gridlet gridlet=null;
		double totalET=0;//�ܴ���ʱ��
		double totalCos=0;//�ܿ���
		double totalLen=0;//�����ܳ���

		String indent="	";
		System.out.println();
		System.out.println("=========="+name+"�����==========");
		/*System.out.println("����ID"+indent+"״̬"+indent+indent+"��ԴID"+
				indent+"����"+indent+indent+indent+"���񳤶�"+
				indent+indent+"����ʱ��"+indent+indent+indent+"���ʱ��");*/

		//��ӡȫ�������ѭ��
		int i=0;
		double et, cos, len;//����ʱ�������������񳤶�
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
				System.out.println("���ʱ��Ϊ��"+gridlet.getFinishTime());
			}
			totalET+=et;
			totalCos+=cos;
			totalLen+=len;
		}

		System.out.println("�ܴ���ʱ��Ϊ��"+totalET+indent+"�ܿ���Ϊ��"+totalCos+indent+"�����ܳ���Ϊ��"+totalLen);
	}

}






















