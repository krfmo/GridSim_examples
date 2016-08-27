package test_algorithm;

/*
 * OBL�㷨���������ƽ���㷨
 * ��������������ܴ�������Դ���п���PE����Դ��
 * 
 * ʵ��������
 * 1.������Դ��ÿ����Դֻ��һ��Machine��ÿ��Machineֻ��һ��PE
 * 2.ÿ����Դ�Ĵ����ٶȸ�����ͬ��mipsRating�ֱ�Ϊ37��39��41����������Դ�����ٶ����
 * 3.ÿ���û�����10���������񳤶Ȳ���ȫ��ͬ
 */
import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimRandom;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

import java.util.Calendar;
import java.util.LinkedList;

public class TestOBL extends GridSim{
	public static final int GRIDLET_NUM=10;//��Ҫ��������������
	
	private Integer ID_;//�û�ID
	private String name_;//�û���
	private int totalResource_;//��Դ����������Ϊ3��
	private GridletList list_;//���񼯺�
	private GridletList receiveList_;//����Դ���ص������б�����ʱ����״̬�Ѹ�
	
	private double[] glLengths={5690.0, 10190, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 10606};//������ɵ����񳤶ȣ������볣��GRIDLET_NUM��Ӧ
/*	private double[] glLengths={569.0, 1019.0, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 1060.6};//������ɵ����񳤶ȣ������볣��GRIDLET_NUM��Ӧ
*/
	public TestOBL(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		this.receiveList_=new GridletList();
		
		//Ϊ�û�ʵ���ȡID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("����һ����Ϊ"+name+"��id="+this.ID_ +"�������û�ʵ��");
		
		//Ϊ�����û��������������б�
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println(name+":���ڴ���"+this.list_.size()+"����������");
	}
	
	@Override
	public void body() {
		int resourceID[]=new int[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];
		//int[] strategy=schedule();	//���Ȳ����㷨�Ȳ��������ȡ������ȡ��������Դ���ڵ�������
		ResourceCharacteristics[] resChars=new ResourceCharacteristics[this.totalResource_];//װ������Դ���Զ���

		LinkedList resList;

		/*
		 * �ȴ���ȡ��Դ�б���GridSim�����ö��̻߳������������������ܱ�������Դע��
		 * ��GISʵ�嵽����硣��ˣ������ȵȴ���
		 */
		while(true){
			//��Ҫ��ͣһ�µȴ�������Դ������GIS��ע��
			super.gridSimHold(1.0);//����1s

			resList=super.getGridResourceList();//GridSim��ķ���
			if(resList.size()==this.totalResource_){
				break;
			}else{
				System.out.println(this.name_+"�����ڵȴ���ȡ��Դ�б�...");
			}
		}
		
		//һ��ѭ�������õ����п�����Դ
		int i=0;
		for(i=0; i<this.totalResource_; i++){
			//��Դ�б�����������ԴID��������Դ����
			resourceID[i]=((Integer)resList.get(i)).intValue();

			//����Դʵ�巢�������Ե�����
			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW,
					GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			//�ȴ���ȡһ����Դ����
			resChars[i]=(ResourceCharacteristics) super.receiveEventObject();
			resourceName[i]=resChars[i].getResourceName();

			System.out.println(this.name_+"���Ѵ���Ϊ"+resourceName[i]+"��id="+resourceID[i]+"����Դ���յ���Դ����");

			//���¼���¼��"stat.txt"�ļ�
			super.recordStatistics("\"��"+resourceName[i]+"���յ���Դ����\"", "");
		}
		
		////////////////////////////
		//������Ҫ�������ɵĵ��Ȳ��Է����������Ӧ��Դ
		schedule(resourceID, resChars);
		
		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars) {
		int i;
		Gridlet gridlet;
		String info;
		
		int resID,index;//���񽫱����䵽����ԴID
		for(i=0; i<GRIDLET_NUM; i++){
			gridlet=this.list_.get(i);
			info="��������_"+gridlet.getGridletID();
			
			while(true){
				index=GridSimRandom.intSample(totalResource_);
				//�����Դ�Ŀ���PE����Ϊ0�������Դ���ã�����ѭ����������Դ�����ã�ѭ����������һ����Դ�±�
				if(resChars[index].getNumFreePE()!=0){
					break;
				}
			}
			
			resID=resourceID[index];
			
			System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
			super.gridletSubmit(gridlet, resID);
			
			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());
			
			this.receiveList_.add(gridlet);
		}
	}
	
	private GridletList getGridletList() {
		return this.receiveList_;
	}

	/**
	 * Ϊ�����û�����GRIDLET_NUM���������񣬷���һ�����񼯺�
	 * ���ù̶�����������������ÿ���û������񳤶�����ͬ��
	 * @param userID	�û�ID
	 * @return һ�����񼯺�
	 */
	private GridletList createGridlet(int userID) {
		GridletList list=new GridletList();
		
		int id=0;//����id��ʼֵ
		//double length=268845.0;//���񳤶�
		long file_size=300;
		long output_size=300;
		
		for(int i=0; i<GRIDLET_NUM;i++){
			Gridlet gridlet=new Gridlet(id+i, glLengths[i], file_size, output_size);
			gridlet.setUserID(userID);//���������һ��...
			list.add(gridlet);
		}
		
		return list;//��Щ���Ǹ��ˣ�����null...
	}
	
////////////////////////��̬����///////////////////////
	
	public static void main(String[] args) {
		System.out.println("��ʼʵ�飡");
		
		try {
			//int num_user=1;//ֻ��һ���û�
			int num_user=3;//��һ��3���û�
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;
			
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//��ʼ��GridSim����
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, 
					exclude_from_processing, report_name);
			
/*			GridResource resource0=createGridResource("Resource_0", 37);
			GridResource resource1=createGridResource("Resource_1", 39);
			GridResource resource2=createGridResource("Resource_2", 41);
*/			GridResource resource0=createGridResource("Resource_0", 30);
			GridResource resource1=createGridResource("Resource_1", 40);
			GridResource resource2=createGridResource("Resource_2", 50);
			
			int total_resource=3;//�����洴����Դ�Ĵ������Ӧ
			
			TestOBL user0=new TestOBL("User_0", 560.00, total_resource);//���������û���ͬʱ���������񼯺�
			TestOBL user1=new TestOBL("User_1", 560.00, total_resource);//���������û���ͬʱ���������񼯺�
			TestOBL user2=new TestOBL("User_2", 560.00, total_resource);//���������û���ͬʱ���������񼯺�
			//�������Ϳ��Ը����������Դ��Ϣ���ɵ��Ȳ����ˣ�����ò�Ʋ���Ӧ����body����ò�������
			
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
			System.out.println("��������");
		}
	}

	/**
	 * ����һ��������Դ��
	 * @param name	һ��������Դ��
	 * @return	һ��������Դ����
	 */
	private static GridResource createGridResource(String name, int mipsRating){
		//1.����һ�������б��������洢һ̨���̨����
		MachineList mList=new MachineList();

		//2.��������
		//int mipsRating=377;
		mList.add(new Machine(0, 1, mipsRating));//ֻ����һ̨������ֻ��һ��PE

		/*//3.���贴������������ظ�����2
		mList.add(new Machine(1, 4, mipsRating));//�ڶ�̨����
		mList.add(new Machine(2, 2, mipsRating));//����̨����*/
		
		//4.����һ����Դ���Զ������洢������Դ����
		String arch="Sun Ultra";
		String os="Solaris";
		double time_zone=9.0;
		double cost=3.0;

		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.TIME_SHARED,
				time_zone, cost);

		//5.��󣬴���������Դ����
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