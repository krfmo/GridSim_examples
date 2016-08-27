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
 * ����ѧϰ�Զ����ķ���ģ��
 */
public class LA1 extends GridSim{

	public static final int GRIDLET_NUM=10;
	private Integer ID_;//�û�ID
	private String name_;//�û���
	private int totalResource_;//��Դ����
	private GridletList list_;//���񼯺�
	private GridletList receiveList_;//����Դ���ص������б�

	private double[] glLengths={5690.0, 10190, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 10606};//������ɵ����񳤶�

	public LA1(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		this.receiveList_=new GridletList();

		//Ϊ�û�ʵ���ȡID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("����һ����Ϊ"+name+"��id="+this.ID_+"�������û�ʵ��");

		//Ϊ�����û��������������б�
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println(name+"�����ڴ���"+this.list_.size()+"����������");
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
		schedule(resourceID, resChars);

		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * ����ѧϰ�Զ������㷨ѡ�񲿷֣����ݻ�������״̬�������ú����㷨
	 * @param resourceID
	 * @param resChars
	 */
	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars){
		int i;
		int j;
		//step1:����ect����
		double[][] ect=new double[GRIDLET_NUM][totalResource_];//������һ���յľ���

		//˫��ѭ�����õ�ECT�����������񳤶Ⱥʹ����ٶ����
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();//���񳤶�
				int rating=resChars[j].getMIPSRating();//�����Դ��������
				ect[i][j]=length/rating;
			}
		}

		//��Դ����ʱ�䣬�������ֵ��Ȳ��Զ���Ҫ���¸������Ӧ�±��ֵ��ֻ����MCT�Ƚ�ʱ��Ҫ�����飬MET�Ƚ�ʱ����Ҫ
		double[] ready=new double[totalResource_];
		Gridlet gridlet;
		String info;
		int resID;

		for(i=0; i<GRIDLET_NUM; i++){
			int sch=0;//������Դ���±�
			double[] result;

			//�ڴ˵��õ����㷨�����ж�һ�£�����һ����������MCT�㷨���������MET�㷨
			if(i%3==0){
				result=MCT(i, ready, ect);//����ֵ�а�����Դ�±�sch���±�Ϊsch����Դ�ľ���ʱ��
			}else if(i%3==1){
				result=MET(i, ect);
			}else{
				result=OBL(i, resChars, ect);
			}
			sch=(int) result[0];
			ready[sch]=result[1];

			//��ʼ�ύ����
			gridlet=this.list_.get(i);
			info="��������_"+gridlet.getGridletID();
			resID=resourceID[sch];

			System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());

			this.receiveList_.add(gridlet);
		}

	}

	private double[] MCT(int i, double[] ready, double[][] ect){
		//int i, j;

		/*//����ect����
		double[][] ect=new double[GRIDLET_NUM][totalResource_];

		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();
				int rating=resChars[j].getMIPSRating();
				ect[i][j]=length/rating;
			}
		}*/

		/*//����MCT����
		double[] ready=new double[totalResource_];

		Gridlet gridlet;
		String info;
		int resID;

		for(i=0; i<GRIDLET_NUM; i++){*/
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

		/*ready[sch]=mct;

			//��ʼ�ύ����
			gridlet=this.list_.get(i);
			info="��������_"+gridlet.getGridletID();
			resID=resourceID[sch];

			System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());

			this.receiveList_.add(gridlet);
		}*/
	}

	private double[] MET(int i, double[][] ect){
		/*int i;
		int j;
		//step1:����ect����
		double[][] ect=new double[GRIDLET_NUM][totalResource_];//������һ���յľ���

		//˫��ѭ�����õ�ECT�����������񳤶Ⱥʹ����ٶ����
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();//���񳤶�
				int rating=resChars[j].getMIPSRating();//�����Դ��������
				ect[i][j]=length/rating;
			}
		}

		Gridlet gridlet;
		String info;
		int resID;//���񽫱����䵽����ԴID

		for(i=0; i<GRIDLET_NUM; i++){*/
		int sch=0;//������Դ�±�
		double met=Double.MAX_VALUE;//һ��doubleֵ������ÿ���������Сִ��ʱ��
		for(int j=0; j<totalResource_; j++){
			if(ect[i][j]<met){
				met=ect[i][j];
				sch=j;
			}
		}

		return new double[]{sch, met};

		/*//��ʼ�ύ����
			gridlet=this.list_.get(i);
			info="��������_"+gridlet.getGridletID();
			resID=resourceID[sch];//strategy[i]�������ǵ�i�����񽫱����䵽����Դ�±�

			System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
			super.gridletSubmit(gridlet, resID);

			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());

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
	 * Ϊ�����û�������������
	 * @param userID	�û�ID
	 * @return	һ�����񼯺�
	 */
	private GridletList createGridlet(int userID){
		GridletList list=new GridletList();

		int id=0;//����id��ʼֵ
		long file_size=300;
		long output_size=300;

		for(int i=0; i<glLengths.length; i++){
			Gridlet gridlet=new Gridlet(id+i, glLengths[i], file_size, output_size);
			gridlet.setUserID(userID);
			list.add(gridlet);
		}

		return list;
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
		System.out.println("����ID"+indent+"״̬"+indent+indent+"��ԴID"+
				indent+"����"+indent+indent+indent+"���񳤶�"+
				indent+indent+"����ʱ��"+indent+indent+indent+"���ʱ��");

		//��ӡȫ�������ѭ��
		int i=0;
		double et, cos, len;//����ʱ�������������񳤶�
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

		System.out.println("�ܴ���ʱ��Ϊ��"+totalET+indent+"�ܿ���Ϊ��"+totalCos+indent+"�����ܳ���Ϊ��"+totalLen);
	}

}






















