package test05;

import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimRandom;
import gridsim.GridSimStandardPE;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;
/*
 * ����������չʾ��һ�������û�����ύ�������񵽶��������Դʵ��
 */

/**
 * Test5�������������񣬲����䷢�������������Դʵ��
 */
class Test5 extends GridSim{

	private Integer ID_;
	private String name_;
	private GridletList list_;
	private GridletList receiveList_;
	private int totalResource_;
	
	/**
	 * ����һ���µ�Test5ʵ��
	 * @param name	�ö����ʵ����
	 * @param baudRate	ͨ���ٶ�
	 * @param total_resource	�ɻ�õ�������Դ��
	 * @throws Exception	�ڳ�ʼ��GridSim��֮ǰ����ʵ���ʵ����Ϊ��ʱ���쳣
	 */
	public Test5(String name, double baudRate, int total_resource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=total_resource;
		this.receiveList_=new GridletList();
		
		//��ʵ��һ��ID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("���ڴ���һ����Ϊ"+name+"��id="+this.ID_+"�������û�ʵ��");
		
		//���������û�����һ�����������б�
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println("���ڴ���"+this.list_.size()+"����������");
	}
	
	/**
	 * ���GridSimʵ���ͨ�ŵĺ��ķ���
	 */
	public void body() {
		int resourceID[]=new int[this.totalResource_];//����Դ�����Ǹ�����ĳ���
		double resourceCost[]=new double[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];
		
		LinkedList resList;
		ResourceCharacteristics resChar;
		
		/*
		 * �ȴ���ȡ��Դ�б�GridSim��ʹ�ö��̻߳�����������󵽴�ʱ����ܱ�
		 * һ������������Դʵ��ע�ᵽ������Ϣ����GIS��ʵ���ʱ���硣��ˣ����
		 * �ȵȴ�
		 */
		while(true){
			//��Ҫ�ȴ�������Դ������GIS��ע��
			super.gridSimHold(1.0);//�ȴ�1��
			
			resList=super.getGridResourceList();
			if(resList.size()==this.totalResource_) break;
			else System.out.println("���ڵȴ���ȡ��Դ�б�...");
		}
		
		int i=0;
		
		//ѭ���õ����пɻ�õ���Դ��������Դ��
		for(i=0; i<this.totalResource_; i++){
			//��Դ�б����������ԴID��������������Դ����
			resourceID[i]=((Integer)resList.get(i)).intValue();
			
			//����Դʵ����������������
			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);
	
			//�ȴ���ȡ��Դ����
			resChar=(ResourceCharacteristics)super.receiveEventObject();
			resourceName[i]=resChar.getResourceName();
			resourceCost[i]=resChar.getCostPerSec();
			
			System.out.println("���յ���"+resourceName[i]+"��������Դ���ԣ���Դid="+resourceID[i]);
			
			//����һ�¼���¼��"stat.txt"�ļ���
			super.recordStatistics("\"Received ResourceCharacteristics " +
                    "from " + resourceName[i] + "\"", "");
		}
		
		Gridlet gridlet;
		String info;
		
		//һ�λ�ȡһ�����������ѭ�������ҽ���������͵�һ��������Դʵ�塣Ȼ��ȴ���Ӧ
		Random random=new Random();//������ֻ����һ��Random����͹���
		int id=0;
		for(i=0; i<this.list_.size(); i++){
			gridlet=(Gridlet)this.list_.get(i);
			info="������Դ"+gridlet.getGridletID();
			
			id=random.nextInt(this.totalResource_);
			System.out.println("���ڷ���"+info+"��"+resourceName[id]+"����Դid="+resourceID[id]);
			
			//����һ�����������ض���"resourceID"��������Դ
			super.gridletSubmit(gridlet, resourceID[id]);
			
			//����ͳ��Ŀ�ģ�����һ�¼��洢��"stat.txt"�ļ�
			super.recordStatistics("\"Submit " + info + " to " +
                    resourceName[id] + "\"", "");
			
			//�ȴ�����Դʵ�巵�ص���������
			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());
			
			//����ͳ��Ŀ�ģ�����һ�¼���¼��"stat.txt"�ļ���
			super.recordStatistics("\"Received " + info +  " from " +
                    resourceName[id] + "\"", gridlet.getProcessingCost());
			
			//���յ�����������洢��һ���µ����������б������
			this.receiveList_.add(gridlet);
		}
		
		//�ص�����ʵ�壬��������ͳ��ʵ�壬��Ϊ����������¼�¼���
		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}
	
	/**
	 * �õ����������б�
	 * @return	һ�����������б�
	 */
	public GridletList getGridletList(){
		return this.receiveList_;
	}
	
	/**
	 * ����8����������
	 * @param userID	ӵ����Щ����������û�ʵ��ID
	 * @return	һ�����������б����
	 */
	private GridletList createGridlet(int userID){
		//����һ���洢�������������
		GridletList list=new GridletList();
		
		int id=0;
		double length=3500.0;
		long file_size=300;
		long output_size=300;
		Gridlet gridlet1=new Gridlet(id, length, file_size, output_size);
		id++;
		Gridlet gridlet2=new Gridlet(id, 5000, 500, 500);
		id++;
		Gridlet gridlet3=new Gridlet(id, 9000, 900, 900);
		
		//������Щ���������ӵ���ߣ����������û�������
		gridlet1.setUserID(userID);
		gridlet2.setUserID(userID);
		gridlet3.setUserID(userID);
		
		//������洢���б���
		list.add(gridlet1);
		list.add(gridlet2);
		list.add(gridlet3);
		
		long seed=11L*13*17*19*23+1;
		Random random=new Random(seed);
		
		GridSimStandardPE.setRating(100);
		
		int count=5;
		for(int i=1; i<count+1; i++){
			length=GridSimStandardPE.toMIs(random.nextDouble()*50);
			file_size=(long) GridSimRandom.real(100, 0.10, 0.40, random.nextDouble());
			output_size=(long) GridSimRandom.real(250, 0.10, 0.50, random.nextDouble());
			
			//�����������񣬽����������û��󶨣���������������񼯺�
			Gridlet gridlet=new Gridlet(id+i, length, file_size, output_size);
			gridlet.setUserID(userID);
			list.add(gridlet);
		}
		
		return list;
	}
	
	////////////////////////��̬����////////////////////////
	
	/**
	 * ����������
	 */
	public static void main(String[] args) {
		System.out.println("��ʼTest5");
		
		try {
			//��һ������ʼ��GridSim���������ڴ����κ�ʵ��֮ǰ��ʼ�������򱨴���
			int num_user=1;//�����û�����
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;//��׷��GridSim�¼�
			
			//���κ�ͳ�ƶ����Ƴ����ļ���������б�
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};
			
			//��д�µı����ļ��������ﲻ��Ҫ��
			String report_name=null;
			
			//��ʼ����
			System.out.println("��ʼ��GridSim��");
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);
			
			//�ڶ���������һ������������Դ����
			GridResource resource0=createGridResource("Resource_0");
			GridResource resource1=createGridResource("Resource_1");
			GridResource resource2=createGridResource("Resource_2");
			int total_resource=3;
			
			//������������Test5����
			Test5 obj=new Test5("Test5", 560.00, total_resource);
			
			//���Ĳ�����ʼ����
			GridSim.startGridSimulation();
			
			//���һ�����������֮���ӡ��������
			GridletList newList=obj.getGridletList();
			printGridletList(newList);
			
			System.out.println("Test5������");
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
	private static GridResource createGridResource(String name){
		System.out.println();
		System.out.println("���ڿ�ʼ����һ������3��machine��������Դ");
		
		//����һ��������Դ�Ĳ�������
		//1.��Ҫ����һ�������б�������洢һ����������
		MachineList mList=new MachineList();
		System.out.println("����һ�������б�");
		
		//2.����һ�������������ǣ�id��PE����MIPS��
		int mipsRating=377;
		mList.add(new Machine(0, 4, mipsRating));//��һ̨����
		System.out.println("������һ������������4��PE�����ҽ�������������б�");
		
		//3.����򴴽�����������ظ�����2
		mList.add(new Machine(1, 4, mipsRating));//�ڶ�̨����
		System.out.println("�����ڶ�������������4��PE�����ҽ�������������б�");
		
		mList.add(new Machine(2, 2, mipsRating));//����̨����
		System.out.println("��������������������2��PE�����ҽ�������������б�");
		
		//4.����һ����Դ���Զ�����������������Դ�����ԣ�ϵͳ�ṹ������ϵͳ�������б�������ԣ�ʱ�乲���ռ乲��ʱ���ͻ���
		String arch="Sun Ultra";
		String os="Solaris";
		double time_zone=9.0;
		double cost=3.0;
		
		//����һ����Դ���Զ�������������Դ���ԣ�ϵͳ�ṹ������ϵͳ�������б�������ԣ�ʱ���Ϳ���
		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.TIME_SHARED,
				time_zone, cost);
		
		System.out.println("������������Դ���ԣ����洢�˻����б�");
		
		//5.�����Ҫ����һ��������Դ����
		double baud_rate=100.0;
		long seed=11L*13*17*19*23+1;
		double peakLoad=0.0;
		double offPeakLoad=0.0;
		double holidayLoad=0.0;
		
		LinkedList Weekends=new LinkedList();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));
		
		LinkedList Holidays=new LinkedList();
		GridResource gridRes=null;
		try {
			//����������Դ
			gridRes=new GridResource(name, baud_rate, seed, 
					resConfig, peakLoad, offPeakLoad,
					holidayLoad, Weekends, Holidays);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("���գ�����һ��������Դ�����洢������Դ������");
		System.out.println();
		
		return gridRes;
	}
	
	/**
	 * ��ӡ�����������
	 * @param list	�����������
	 */
	private static void printGridletList(GridletList list){
		int size=list.size();
		Gridlet gridlet;
		
		String indent="	";
		System.out.println();
		System.out.println("==========���==========");
		System.out.println("��������ID"+indent+indent+"״̬"+indent+"��ԴID"+indent+"����");
		
		for(int i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.print(gridlet.getGridletID()+indent+indent);
			
			if(gridlet.getGridletStatus()==Gridlet.SUCCESS){
				System.out.print("�ɹ�");
			}
			
			System.out.println(indent+gridlet.getResourceID()+indent+gridlet.getProcessingCost());
		}
	}
	
}
