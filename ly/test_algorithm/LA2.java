package test_algorithm;

import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimRandom;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.MyGridletQueue;
import gridsim.ResourceCharacteristics;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

/*
 * ����ѧϰ�Զ����ķ���ģ��
 * ���񵽴ﰴ���ɷֲ������񳤶Ȱ���˹�ֲ�
 */
public class LA2 extends GridSim{

	//����
	public static final int GRIDLET_NUM=100;
	//���ɳ��ȷ��Ӹ�˹�ֲ�������ʱ�õ��ľ�ֵ�ͷ���Ĳ���
	public static final int GAUSSIAN_CONST=10000;

	//��Ա����
	private Integer ID_;//�û�ID
	private String name_;//�û���
	private int totalResource_;//��Դ����
	private MyGridletQueue queue_;//���񼯺�
	private MyGridletQueue receiveQueue_;//����Դ���ص������б�

	//���񳤶����飬����Ϊ���Ӹ�˹�ֲ���intֵ��������Ϊ����GRIDLET_NUM
	//����ֻ�ܱ�֤����һ�θ����û�������ͬ�����񼯺ϣ����ǲ�ͬ�����е����񼯺��ǲ�ͬ�ģ��Ժ��б�Ҫ���԰�һ�����񳤶����ݱ��棬Ȼ��ÿ����ͬһ������
	private int[] glLengths=createGL();

	public LA2(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		//�Ȳ��Ķ����ڴ����û�ʱ�ʹ������մ���󷵻ص��������
		this.receiveQueue_=new MyGridletQueue();

		//Ϊ�û�ʵ���ȡID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("����һ����Ϊ"+name+"��id="+this.ID_+"�������û�ʵ��");

		//Ϊ�����û��������������б�
		this.queue_=createGridlet(this.ID_.intValue());
		System.out.println(name+"�����ڴ���"+this.queue_.size()+"����������");
	}

	/**
	 * ������������ķ���
	 * ����GRIDLET_NUM�����Ӹ�˹�ֲ�������
	 * 
	 * @return ����ֵΪһ��int���飬���鳤��ΪGRIDLET_NUM
	 */
	private int[] createGL() {
		Random random=new Random();
		int[] glls=new int[GRIDLET_NUM];
		for(int i=0; i<GRIDLET_NUM; i++){
			glls[i]=(int) (Math.sqrt(GAUSSIAN_CONST)*random.nextGaussian()+GAUSSIAN_CONST);
		}
		return glls;
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
				System.out.println(this.name_+"�����ڵȴ���ȡ��Դ�б�...");
			}
		}

		for(int i=0; i<this.totalResource_; i++){
			resourceID[i]=((Integer)resList.get(i)).intValue();

			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			resChars[i]=(ResourceCharacteristics) super.receiveEventObject();
			resourceName[i]=resChars[i].getResourceName();

			System.out.println(this.name_+"���Ѵ���Ϊ"+resourceName[i]+"��id="+resourceID[i]+"����Դ���յ���Դ����");

			super.recordStatistics("\"��"+resourceName[i]+"���յ���Դ����\"", "");
		}

		////////////
		//������Ҫ���յ��Ȳ��Է����������Ӧ��Դ
		try {
			schedule(resourceID, resChars);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars) throws Exception{
		int i;
		int j;
		//step1:����ect����
		double[][] ect=new double[GRIDLET_NUM][totalResource_];//������һ���յľ���

		//˫��ѭ�����õ�ECT�����������񳤶Ⱥʹ����ٶ����
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				//double length=queue_.take().getGridletLength();//���񳤶�
				//���ˣ�ֱ��������������ETC����ɣ���̬������Щ
				int length=glLengths[i];
				int rating=resChars[j].getMIPSRating();//�����Դ��������
				ect[i][j]=length/rating;
			}
		}

		//��Դ����ʱ�䣬�������ֵ��Ȳ��Զ���Ҫ���¸������Ӧ�±��ֵ��ֻ����MCT�Ƚ�ʱ��Ҫ�����飬MET�Ƚ�ʱ����Ҫ
		double[] ready=new double[totalResource_];
		Gridlet gridlet;
		//String info;
		int resID;

		for(i=0; i<GRIDLET_NUM; i++){
			int sch=0;//������Դ���±�
			double[] result;

			//�ڴ˵��õ����㷨�����ж�һ�£�����һ����������MCT�㷨���������MET�㷨
			/*if(i%2==0){
				result=MCT(i, ready, ect);//����ֵ�а�����Դ�±�sch���±�Ϊsch����Դ�ľ���ʱ��
			}else{
				result=OBL(i, resChars, ect);
			}*/
			/*if(i<=60){
				result=MCT(i, ready, ect);//����ֵ�а�����Դ�±�sch���±�Ϊsch����Դ�ľ���ʱ��
			}else if(i<=70){
				result=MET(i, ect);
			}else{
				result=OBL(i, resChars, ect);
			}*/

			//�������Խ����MCT>OBL>MET��MCT��OBL��Ͻ������
			result=MCT(i, ready, ect);
			//result=MET(i, ect);
			//result=OBL(i, resChars, ect);
			sch=(int) result[0];
			ready[sch]=result[1];

			//��ʼ�ύ����
			//ȡ�����Ƴ�����Ԫ��
			gridlet=this.queue_.take();
			//info="��������_"+gridlet.getGridletID();
			resID=resourceID[sch];

			//System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			//System.out.println("���ڽ�����������"+gridlet.getGridletID());

			this.receiveQueue_.add(gridlet);
		}

	}

	private double[] MCT(int i, double[] ready, double[][] ect){
		int sch=0;//������Դ���±�
		double mct=Double.MAX_VALUE;

		//ʵ�����ڲ�ѭ�������ݲ��ǵ����㷨�����������õ�����
		for(int j=0; j<totalResource_; j++){
			double a=ect[i][j]+ready[j];

			if(a<mct){
				mct=a;
				sch=j;
			}
		}

		return new double[]{sch, mct};
	}

	private double[] MET(int i, double[][] ect){
		int sch=0;//������Դ�±�
		double met=Double.MAX_VALUE;//һ��doubleֵ������ÿ���������Сִ��ʱ��
		for(int j=0; j<totalResource_; j++){
			if(ect[i][j]<met){
				met=ect[i][j];
				sch=j;
			}
		}

		return new double[]{sch, met};

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

	private MyGridletQueue getGridletQueue(){
		return this.receiveQueue_;
	}

	/**
	 * Ϊ�����û�������������
	 * @param userID	�û�ID
	 * @return	һ���������
	 */
	private MyGridletQueue createGridlet(int userID){
		final MyGridletQueue queue=new MyGridletQueue();

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
			try {
				queue.put(gridlet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return queue;
	}

	////////////////////////��̬����////////////////////////
	public static void main(String[] args) {
		System.out.println("��ʼʵ�飡");

		try{
			int num_user=3;//�û�����Ϊ3
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;

			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//��ʼ��GridSim��
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);

			//������ˣ�����ϵͳ��ϵͳ�ṹ����Դû��Ӱ�죬��ҪӰ����Դ���ܵ�����Դ��������MIPSRating
			GridResource resource0=createGridResource("Resource0", 30, "SGI Origin", "Irix");
			GridResource resource1=createGridResource("Resource1", 40, "SGI Origin", "Irix");
			GridResource resource2=createGridResource("Resource2", 50, "SGI Origin", "Irix");
			/*GridResource resource0=createGridResource("Resource0", 30, "Sun Ultra", "Solaris");
			GridResource resource1=createGridResource("Resource1", 40, "Sun Ultra", "Solaris");
			GridResource resource2=createGridResource("Resource2", 50, "Sun Ultra", "Solaris");*/

			int total_resource=3;//���洴����3����Դ������������3
			LA2 user0=new LA2("User_0", 560.00, total_resource);
			LA2 user1=new LA2("User_1", 560.00, total_resource);
			LA2 user2=new LA2("User_2", 560.00, total_resource);

			GridSim.startGridSimulation();

			MyGridletQueue newQueue=null;
			newQueue=user0.getGridletQueue();
			printGridletList(newQueue, "User_0");
			newQueue=user1.getGridletQueue();
			printGridletList(newQueue, "User_1");
			newQueue=user2.getGridletQueue();
			printGridletList(newQueue, "User_2");
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

	private static void printGridletList(MyGridletQueue queue, String name) {
		int size=queue.size();
		Gridlet gridlet=null;
		double totalET=0;//�ܴ���ʱ��
		double totalCos=0;//�ܿ���
		double totalLen=0;//�����ܳ���

		String indent="	";
		System.out.println();
		System.out.println("=========="+name+"�����==========");
		System.out.println("����ID"+indent+"״̬"+indent+indent+"��ԴID"+
				indent+"����"+indent+indent+indent+"���񳤶�"+
				indent+indent+"����ʱ��"+indent+indent+indent+"���ʱ��");

		//��ӡȫ�������ѭ��
		int i=0;
		double et, cos, len;//����ʱ�������������񳤶�
		for(i=0; i<size; i++){
			try {
				//�Ӷ���ȡ������
				gridlet=(Gridlet)queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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

		System.out.println("�ܴ���ʱ��Ϊ��"+totalET+indent+"�ܿ���Ϊ��"+totalCos+indent+"�����ܳ���Ϊ��"+totalLen);
	}

}






















