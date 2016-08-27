package test04;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

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
 * ����������չʾ��һ�������û��������һ��������Դʵ���ύ��������ġ�
 */

/**
 * Test4�ഴ�����������ҽ����Ƿ�����һ��������Դʵ�塣
 */
class Test4 extends GridSim{

	private Integer ID_;
	private String name_;
	private GridletList list_;
	private GridletList receiveList_;

	/**
	 * ����һ��Test4����
	 * @param name	�ö����ʵ����
	 * @param baudRate	ͨ���ٶ�
	 * @throws Exception	��ʼ��֮ǰ����ʵ���ʵ����Ϊ��ʱ�����쳣
	 */
	Test4(String name, double baudRate) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.receiveList_=new GridletList();

		//Ϊʵ��ָ��ID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("����һ����Ϊ"+name+"�������û�ʵ�壬id="+this.ID_);

		//Ϊ�����û�����һ�����������б�
		this.list_=createGridlet(this.ID_.intValue());
		System.out.println("���ڴ���"+this.list_.size()+"����������");
	}

	/**
	 * ����GridSimʵ���ͨ�ŵĺ��ķ���
	 */
	public void body() {
		int resourceID=0;
		String resourceName;
		LinkedList resList;
		ResourceCharacteristics resChar;

		/*
		 * �ȴ��õ���Դ�б�
		 * ����GridSim��ʹ�ö��̻߳��������������ܻ��һ������������Դʵ�彫��ע�ᵽ������Ϣ����GIS��ʵ�嵽����硣
		 * ��ˣ�������ô��ڵȴ�״̬
		 */
		while(true){
			//��Ҫ��ͣһ�£����ȴ�������Դ�����GIS��ע��
			super.gridSimHold(1.0);//�ȴ�1��
			resList=super.getGridResourceList();
			if(resList.size()>0){
				//�ڱ����У�����֪������Ҫ����һ����Դ����Դ�б��������ԴId���б���������Դ����
				Integer num=(Integer) resList.get(0);
				resourceID=num.intValue();

				//����Դʵ����������������
				super.send(resourceID, GridSimTags.SCHEDULE_NOW,
						GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);

				//�ȴ��õ�һ��������Դ����
				resChar=(ResourceCharacteristics) super.receiveEventObject();
				resourceName=resChar.getResourceName();

				System.out.println("�յ���Դ��Ϊ"+resourceName+"����Դ����,id="+resourceID);

				//���¼���¼��"stat.txt"�ļ�
				super.recordStatistics("\"Received ResourceCharacteristics " +
                        "from " + resourceName + "\"", "");

				break;
			}
			else
				System.out.println("�ȴ���ȡ��Դ�б�...");
		}

		Gridlet gridlet;
		String info;

		//һ�λ�ȡһ�����������ѭ���������䷢�͵�һ��������Դʵ�塣Ȼ��ȴ���Ӧ
		for(int i=0; i<this.list_.size(); i++){
			gridlet=(Gridlet)this.list_.get(i);
			info="��������_"+gridlet.getGridletID();

			System.out.println("����"+info+"����Դ"+resourceName+"����ԴID��"+resourceID);

			//��һ������������������ָ����ԴID��������Դ
			super.gridletSubmit(gridlet, resourceID);

			//��һ�ַ���������������ʵ��ķ���
			//super.send(resourceID, GridSimTags.SCHEDULE_NOW, GridSimTags.GRIDLET_SUBMIT, gridlet);

			//����ͳ��Ŀ�Ľ��¼���¼��"stat.txt"
			super.recordStatistics("\"Submit " + info + " to " + resourceName +
                    "\"", "");

			//�ȴ�����Դʵ�巵�ص���������
			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());

			//����ͳ��Ŀ�Ľ��¼���¼��"GridSim_stat.txt"�ļ�
			super.recordStatistics("\"Received " + info +  " from " +
                    resourceName + "\"", gridlet.getProcessingCost());

			//���յ�����������洢���µ����������б������
			this.receiveList_.add(gridlet);
		}

		/*
		 * �ص�����ʵ�壬����GridStatisticsʵ�壬��Ϊ���������洢�ض��¼����š�����
		 */
		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * �õ����������б�
	 * @return	���������б�
	 */
	public GridletList getGridletList(){
		return this.receiveList_;
	}

	/**
	 * ������������
	 * @param userID	ӵ����Щ����������û�ʵ��ID
	 * @return	һ�����������б����
	 */
	private GridletList createGridlet(int userID){
		//����һ������ʢ����������
		GridletList list=new GridletList();

		//�ֶ�����3����������
		int id=0;
		double length=3500.0;
		long file_size=300;
		long output_size=300;
		Gridlet gridlet1=new Gridlet(id, length, file_size, output_size);
		id++;
		Gridlet gridlet2=new Gridlet(id, 5000, 500, 500);
		id++;
		Gridlet gridlet3=new Gridlet(id, 9000, 900, 900);

		//������Щ�����ӵ����
		gridlet1.setUserID(userID);
		gridlet2.setUserID(userID);
		gridlet3.setUserID(userID);

		//�洢���б�
		list.add(gridlet1);
		list.add(gridlet2);
		list.add(gridlet3);

		//�÷�������5����������
		long seed=11L*13*17*19*23+1;
		Random random=new Random(seed);

		//���ô�������MIPS Rating
		GridSimStandardPE.setRating(100);

		//����5����������
		int count=5;
		for(int i=1; i<count+1; i++){
			//���񳤶������ֵ�͵�ǰPE����������MIPS Rating������
			length=GridSimStandardPE.toMIs(random.nextDouble()*50);

			//�涨�������ļ��ĳ��ȵı仯��Χ�ǣ�100 + (10% to 40%)
			file_size=(long) GridSimRandom.real(100, 0.10, 0.40, random.nextDouble());

			//�涨������������ȵı仯��Χ�ǣ�250 + (10% to 50%)
			output_size=(long) GridSimRandom.real(250, 0.10, 0.50, random.nextDouble());

			//����һ���µ������������
			Gridlet gridlet=new Gridlet(id+i, length, file_size, output_size);

			gridlet.setUserID(userID);

			//����������񵽼���
			list.add(gridlet);
		}

		return list;
	}

	//////////////////��̬����//////////////////

	/**
	 * ����������
	 */
	public static void main(String[] args) {
		System.out.println("��ʼTest4");

		try {
			/*��һ������ʼ��GridSim����Ӧ���ڴ����κ�ʵ��֮ǰ���ø÷�����
			 * ���ǲ�����û�г�ʼ��GridSim֮ǰʹ��������Դ�������ᵼ��run-time�쳣*/

			/*
			 * �走�����������˺ü���Ĵ�������Ȼ���û������ˣ�����
			 * ��Ȼ����ȫ������ճ��������
			 * ϸ�ĵ�����𣿣���
			 * ��ϸ����Ļ����ģ�����
			 * ��Ϊ��0��1���������۶�����˰�ι������
			 * ����Ҳ����������Ĵ�����
			 */
			int num_user=1;//�û�����Ҫ�������������У���Ϊ����Ҫ�����û�ʵ�壬���Խ�ֵ��Ϊ0
			Calendar calendar=Calendar.getInstance();//�ڲ���ʱ����������¼ģ��Ŀ�ʼ�ͽ���ʱ��
			boolean trace_flag=true;//һ�����Կ��أ�ֵΪ���ʾ��Ҫ���ټ�¼GridSimģ���ÿһ��

			//list of files or processing names to be excluded from any statistical measures
			//��ͳ�ƹ����У����������ڵ��ļ����ƺʹ������
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};

			String report_name=null;//�������ƣ���������Ҫд���棬���Բ��������õ�ReportWriter�������

			//��ʼ��GridSim��
			System.out.println("��ʼ��GridSim��");
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file,
					exclude_from_processing, report_name);

			//�ڶ���������һ��������Դ����
			String name="Resource_0";
			GridResource resource=createGridResource(name);

			//������������һ��Test4����
			Test4 obj=new Test4("Test4", 560.00);

			//���Ĳ�����ʼģ��
			GridSim.startGridSimulation();

			//���һ����ģ�����ʱ��ӡ��������
			GridletList newList=obj.getGridletList();
			printGridletList(newList);

			System.out.println("Test4����");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("��������");
		}
	}

	/**
	 * ����һ��������Դ��һ��������Դ����һ������������Machine����
	 * ���Ƶģ�һ����������һ������PE������Ԫ��CPU����
	 * �ڱ����У�����ģ���������Դ��������������ÿ����������һ������PE��
	 * @param name	һ��������Դ����
	 * @return	һ��������Դ����
	 */
	private static GridResource createGridResource(String name){
		System.out.println();
		System.out.println("��ʼ��������3��machine��������Դ...");

		//�����Ǵ���������Դ���裺
		/*1.����һ�������б����ڴ洢һ����������*/
		MachineList mList=new MachineList();
		System.out.println("����һ�������б�");

		/*2.����һ�������������ֱ��ǣ�����id��PE������MIPS rating���������ļ���������
		 * �����У���ʹ����Դ����ϢΪ��hpc420.hpcc.jp, AIST, Tokyo, Japan
		 * NOTE��these data are taken the from GridSim paper, page 25.
		 * �����У�����PE������ͬ��MIPS(Millions Instruction Per Second) Rating
		 * ����ÿ��PE����������ͬ
		 * */
		int mipsRating=377;
		mList.add(new Machine(0, 4, mipsRating));//��һ̨����
		System.out.println("������һ̨����������������Ϊ4���ѽ����������б�");
		/*3.�����Ҫ��������������ظ��ڶ�����
		 * �ڱ����У��ձ���AIST��3��������ÿ��������MIPS����ͬ��PE������ͬ
		 * NOTE�������ֻ��ҪΪÿһ��������Դ����һ����������ô����ʡ���ⲽ
		 * */
		mList.add(new Machine(1, 4, mipsRating));//�ڶ�̨����
		System.out.println("�����ڶ�̨����������������Ϊ4���ѽ����������б�");

		mList.add(new Machine(2, 2, mipsRating));//����̨����
		System.out.println("��������̨����������������Ϊ2���ѽ����������б�");

		/*4.����һ����Դ���Զ��������洢������Դ���ԣ�
		 * ϵͳ��ϵ�ṹ������ϵͳ�������б�������ԣ�ʱ��/�ռ乲��ʱ���ʹ��ۣ�G$/PE time unit��
		 * */
		String arch="Sun Ultra";//ϵͳ��ϵ�ṹ
		String os="Solaris";//����ϵͳ
		double time_zone=9.0;//��Դ����ʱ��
		double cost=3.0;//ʹ�ø���Դ�Ĵ���

		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.TIME_SHARED, time_zone, cost);
		System.out.println("������������Դ���Զ��󣬲��洢�˻����б�");

		//5.���գ�������Ҫ����һ���������
		double baud_rate=100.0;//ͨ���ٶȣ���������
		long seed=11L*13*17*19*23+1;//???
		double peakLoad=0.0;//�߷�ʱ����Դ����
		double offPeakLoad=0.0;//�Ǹ߷�ʱ����Դ����
		double holidayLoad=0.0;//����ʱ����Դ����

		//�൱������ĩ��������Դһ�ܹ���7��
		LinkedList<Integer> Weekends=new LinkedList<>();
		Weekends.add(new Integer(Calendar.SATURDAY));
		Weekends.add(new Integer(Calendar.SUNDAY));

		//�޼��ա�Ȼ���������в�û�����ü���
		LinkedList<Integer> Holidays=new LinkedList<>();

		GridResource gridRes=null;

		try {
			gridRes=new GridResource(name, baud_rate, seed,
					resConfig, peakLoad, offPeakLoad, holidayLoad, Weekends, Holidays);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		System.out.println("��󣬴���һ��������Դ���洢�˸�������Դ������");
		System.out.println();

		return gridRes;
	}

	/**
	 * ��ӡ�����������
	 * @param list	���������б�
	 */
	private static void printGridletList(GridletList list){
		int size=list.size();
		Gridlet gridlet;

		String indent="	";
		System.out.println();
		System.out.println("==========���==========");
		System.out.println("����ID"+indent+"״̬"+indent+"��ԴID"+indent+"����");

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


















