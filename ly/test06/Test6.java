package test06;

import java.util.Calendar;
import java.util.LinkedList;

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

/*
 * ����������չʾ��һ�������û�����ύ�������񵽶��������Դʵ��
 */

/**
 * Test6��
 */
public class Test6 extends GridSim{

	private Integer ID_;
	private String name_;
	private GridletList list_;
	private GridletList receiveList_;
	private int totalResource_;

	/**
	 * ����һ���µ�Test6����
	 * @param name	�ö����ʵ������
	 * @param baudRate	ͨ���ٶ�
	 * @param totalResource	�ɻ�õ�������Դ����
	 * @throws Exception	���ڳ�ʼ��GridSim��֮ǰ����ʵ���ʵ����Ϊ�������쳣
	 */
	public Test6(String name, double baudRate, int totalResource) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=totalResource;
		this.receiveList_=new GridletList();

		//Ϊʵ���ȡһ��ID
		this.ID_ =new Integer(getEntityId(name));
		System.out.println("����һ����Ϊ"+name+"��id="+this.ID_ +"�������û�ʵ��");

		//Ϊ�������û�����һ�����������б�
		this.list_=createGridlet(this.ID_ .intValue());
		System.out.println(name+":���ڴ���"+this.list_.size()+"����������");
	}

	/**
	 * ����GridSimʵ���ͨ�ŵĺ��ķ���
	 */
	public void body(){
		int resourceID[]=new int[this.totalResource_];
		double resourceCost[]=new double[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];

		LinkedList resList;
		ResourceCharacteristics resChar;

		/*
		 * �ȴ���ȡ��Դ�б�GridSim�����ö��̻߳������������������ܱ�������Դע��
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
			//��Դ�б����������ԴID��������Դ����
			resourceID[i]=((Integer)resList.get(i)).intValue();

			//����Դʵ�巢�������Ե�����
			super.send(resourceID[i], GridSimTags.SCHEDULE_NOW,
					GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

			//�ȴ���ȡһ����Դ����
			resChar=(ResourceCharacteristics) super.receiveEventObject();
			resourceName[i]=resChar.getResourceName();
			resourceCost[i]=resChar.getCostPerSec();

			System.out.println(this.name_+"���Ѵ���Ϊ"+resourceName[i]+"��id="+resourceID[i]+"����Դ���յ���Դ����");

			//���¼���¼��"stat.txt"�ļ�
			super.recordStatistics("\"��"+resourceName[i]+"���յ���Դ����\"", "");
		}

		Gridlet gridlet;
		String info;

		//һ�λ�ȡһ���������񣬲����䷢���������������Դʵ�壬Ȼ��ȴ��ظ�
		int id=0;
		for(i=0; i<this.list_.size(); i++){
			gridlet=(Gridlet)this.list_.get(i);
			info="��������_"+gridlet.getGridletID();

			//��5�����õ���Random��������������±�ֵ
			id=GridSimRandom.intSample(this.totalResource_);
			System.out.println(this.name_+"�����ڷ���"+info+"����Ϊ"+resourceName[id]+"��id="+resourceID[id]+"����Դ");

			//����һ�����������ض���"resourceID"������Դ
			super.gridletSubmit(gridlet, resourceID[id]);

			//����ͳ��Ŀ�ģ����¼���¼��"stat.txt"�ļ�
			super.recordStatistics("\"�ύ"+info+"����Ϊ"+resourceName[id]+"����Դ\"", "");

			//�ȴ�����Դʵ���յ�һ����������
			gridlet=super.gridletReceive();
			System.out.println(this.name_+"�����ڽ�����������"+gridlet.getGridletID());

			//����ͳ��Ŀ�Ľ��¼���¼��"stat.txt"�ļ�
			super.recordStatistics("\"���յ���"+resourceName[id]+"������"+info+"\"", gridlet.getProcessingCost());

			//���յ�����������洢��һ���µ����������б����
			this.receiveList_.add(gridlet);
		}

		//�ر�ʵ��
		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
		System.out.println(this.name_+":%%%%�˳�body()");
	}


	/**
	 * �õ����������б�
	 * @return	һ�����������б�
	 */
	public GridletList getGridletList(){
		return this.receiveList_;
	}

	/**
	 * ������������
	 * @param userID	�������������û�ʵ��ID
	 * @return	һ�����������б����
	 */
	private GridletList createGridlet(int userID){
		//����һ���������洢��������
		GridletList list=new GridletList();

		//�ֶ�����3������
		int id=0;
		double length=3500.0;
		long file_size=300;
		long output_size=300;
		Gridlet gridlet1=new Gridlet(id, length, file_size, output_size);
		id++;
		Gridlet gridlet2=new Gridlet(id, 5000, 500, 500);
		id++;
		Gridlet gridlet3=new Gridlet(id, 9000, 900, 900);

		//�����������������
		gridlet1.setUserID(userID);
		gridlet2.setUserID(userID);
		gridlet3.setUserID(userID);

		//�洢���񵽼���
		list.add(gridlet1);
		list.add(gridlet2);
		list.add(gridlet3);

		//����GridSimRandom��GridSimStandardPE�ഴ��5������

		//����PE��MIPS��
		GridSimStandardPE.setRating(100);

		//������������������������5��
		int max=5;
		int count=GridSimRandom.intSample(max);//����������������ĸ���
		for(int i=1; i<count+1; i++){
			//�������񳤶�ȡ�������ֵ��PE��ǰMIPS��
			length=GridSimStandardPE.toMIs(GridSimRandom.doubleSample()*50);

			//���������ļ���С�ڷ�Χ�ڱ仯	100+��10% ��40%��
			file_size=(long)GridSimRandom.real(100, 0.10, 0.40, 
					GridSimRandom.doubleSample());

			//�������������С�ڷ�Χ�ڱ仯	100+��10%��50%��
			output_size=(long)GridSimRandom.real(250, 0.10, 0.50, 
					GridSimRandom.doubleSample());

			//����һ�������������
			Gridlet gridlet=new Gridlet(id+i, length, file_size, output_size);

			gridlet.setUserID(userID);

			//������񵽼���
			list.add(gridlet);
		}

		return list;
	}

	////////////////////////��̬����///////////////////////

	/**
	 * ����������
	 */
	public static void main(String[] args) {
		System.out.println("��ʼTest6");

		try {
			//��һ������ʼ��GridSim�����ڴ����κ�ʵ��֮ǰ���á�
			//û��ʼ��֮ǰ�����������У�����run-time�쳣
			int num_user=3;//�����û���
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=false;

			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;

			//��ʼ��GridSim����
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, 
					exclude_from_processing, report_name);

			//�ڶ���������һ������������Դ����
			GridResource resource0=createGridResource("Resource_0");
			GridResource resource1=createGridResource("Resource_1");
			GridResource resource2=createGridResource("Resource_2");

			int total_resource=3;

			//�����������������û�
			Test6 user0=new Test6("User_0", 560.00, total_resource);
			Test6 user1=new Test6("User_1", 250.00, total_resource);
			Test6 user2=new Test6("User_2", 150.00, total_resource);

			//���Ĳ�����ʼģ��
			GridSim.startGridSimulation();

			//���һ�������������ӡ��������
			GridletList newList=null;
			newList=user0.getGridletList();
			printGridletList(newList, "User_0");

			newList=user1.getGridletList();
			printGridletList(newList, "User_1");

			newList=user2.getGridletList();
			printGridletList(newList, "User_2");

			System.out.println("����Test6");

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
		//1.����һ�������б�������洢һ̨���̨����
		MachineList mList=new MachineList();

		//2.��������
		int mipsRating=377;
		mList.add(new Machine(0, 4, mipsRating));//��һ̨����

		//3.���贴������������ظ�����2
		mList.add(new Machine(1, 4, mipsRating));//�ڶ�̨����
		mList.add(new Machine(2, 2, mipsRating));//����̨����

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

	/**
	 * ��ӡ�����������
	 * @param list	���������б�
	 * @param name	�û���
	 */
	private static void printGridletList(GridletList list, String name){
		int size=list.size();
		Gridlet gridlet;

		String indent="	";
		System.out.println();
		System.out.println("=========="+name+"�����==========");
		System.out.println("��������ID"+indent+"״̬"+indent+"��ԴID"+indent+"����");

		for(int i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.print(gridlet.getGridletID()+indent);

			if(gridlet.getGridletStatus()==Gridlet.SUCCESS){
				System.out.print("�ɹ�");
			}

			System.out.println(indent+gridlet.getResourceID()+indent+gridlet.getProcessingCost());
		}
	}

}











