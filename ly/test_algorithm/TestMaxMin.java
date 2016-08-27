package test_algorithm;
/*
 * Max-Min�㷨
 * �����ECT�����ҳ��������ʱ������������Դ�ԣ���������������Դ����
 * 
 * �����ect����
 * ѭ����ֱ���������񶼱����ȣ�ѭ�����������Σ���һ�ε���һ������
 * �����δ���ȵ������������ʱ�䣻
 * ѡ��������ʱ������������е��ȣ���������Դ����ʱ�䣻
 * 
 * ʵ��������
 * 1.������Դ��ÿ����Դֻ��һ��Machine��ÿ��Machineֻ��һ��PE
 * 2.ÿ����Դ�Ĵ����ٶȸ�����ͬ��mipsRating�ֱ�Ϊ37��39��41����������Դ�����ٶ����
 * 3.ÿ���û�����10���������񳤶Ȳ���ȫ��ͬ��������������MCT�㷨������Min-Min�㷨������ͬ��
 */
import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCharacteristics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TestMaxMin extends GridSim{
	public static final int GRIDLET_NUM=10;//��Ҫ��������������
	//private static List<GridResource> resourceList=new ArrayList<>();//��Դ�����Ǿ�̬�ģ������û�������Щ��Դ

	private Integer ID_;//�û�ID
	private String name_;//�û���
	private int totalResource_;//��Դ����������Ϊ3��
	private GridletList list_;//���񼯺�
	private GridletList receiveList_;//����Դ���ص������б���ʱ����״̬�Ѹ�

	/*private double[] glLengths={569.0, 10190, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 10606};//������ɵ����񳤶ȣ������볣��GRIDLET_NUM��Ӧ
*/	private double[] glLengths={569.0, 1019.0, 949.8, 481.7, 226.4, 827.1, 897.8,
			734.7, 319.6, 1060.6};//������ɵ����񳤶ȣ������볣��GRIDLET_NUM��Ӧ

	public TestMaxMin(String name, double baudRate, int totalResource) throws Exception {
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
		ResourceCharacteristics[] resChars=new ResourceCharacteristics[this.totalResource_];//װ������Դ���Զ���

		LinkedList resList;

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
		int i;
		for(i=0; i<this.totalResource_; i++){
			//��Դ�б����������ԴID��������Դ����
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

		/////////////////////////////////////
		//����Ϊ�����㷨����
		//˫��ѭ�����õ�ECT����ֻ�����񳤶Ⱥʹ����ٶ����
		schedule(resourceID, resChars);

		super.shutdownGridStatisticsEntity();
		super.shutdownUserEntity();
		super.terminateIOEntities();
	}

	/**
	 * ��������㷨����
	 * ����ʵ�������㷨ΪMax_Min�㷨
	 * @param resourceID	��ԴID�б����飬�ڲ�����������Դ��ID������ʱ�Ὣ������ȸ���Ӧ�±����Դ
	 * @param resChars	��Դ���Զ��󼯺ϣ�����ͨ���ò�����ȡ����Դ�Ĵ�������
	 */
	private void schedule(int[] resourceID, ResourceCharacteristics[] resChars) {
		int i;
		int j;
		double[][] ect=new double[GRIDLET_NUM][totalResource_];//������һ���յľ���
		for(i=0; i<GRIDLET_NUM; i++){
			for(j=0; j<totalResource_; j++){
				double length=list_.get(i).getGridletLength();//���񳤶�
				int rating=resChars[j].getMIPSRating();//�����Դ��������
				ect[i][j]=length/rating;
			}
		}
		double[] ready=new double[totalResource_];//���ÿ����Դ����ʱ���double���飬ÿ������һ�����񣬶�Ӧ����Դ����ʱ�䶼Ҫ����
		int[] res=new int[GRIDLET_NUM];//��¼ÿ�������Ӧ����Դ�±�

		//������Ҫ���յ��Ȳ��Է����������Ӧ��Դ
		Gridlet gridlet;
		String info;
		int resID;//���񽫱����䵽����ԴID

		List<Integer> submitted=new ArrayList<>();//�洢�Ѿ������˵������±�
		
		//���ѭ������������ÿ��ѭ����Ҫ���¼���min���飬������洢ÿ����������ʱ����Сֵ����������Сֵ��Ӧ����Դ�±�洢��res���飬
		//�ҳ�min�е����ֵ�����ȸ�ֵ��Ӧ�±������
		while(submitted.size()<GRIDLET_NUM){
			double[] min=new double[GRIDLET_NUM];//��¼ÿ��������������ʱ��
			
			//���˫��ѭ����Ϊ�˸�min[]���鸳ֵ���������ѵ�����ֱ����Ϊ0��������㲢�ж��ĸ���Դ����������ܻ���������ʱ��
			for(i=0; i<GRIDLET_NUM; i++){
				//��������ѵ���
				if(submitted.contains(i)){
					//����������Ѿ����ȣ������������ʱ����Ϊ0����Ϊ����ʱȡ���ֵ����˽���ֵ��Ϊ0һ�����ᱻ�ٴε��ȣ�
					min[i]=0.0;
					continue;
				}
				
				//���������û�б����ȹ�����������������ʱ��
				double minTime=Double.MAX_VALUE;
				for(j=0; j<totalResource_; j++){
					double a=ect[i][j]+ready[j];//a�����������Դ�Ե����ʱ�䣬������min[i]�Ƚϴ�С
					if(a<minTime){
						minTime=a;//������С���ʱ��
						res[i]=j;//��Ӧ����Դ�±�
					}
				}
				min[i]=minTime;
			}
			
			//��ʱ�Ѿ��õ��������ʱ�����飬�Ϳ��ԱȽϳ����ֵ��
			double max=0;//�洢�������ʱ������ֵ
			int index=0;//��¼����Ҫ���ȵ������±�
			for(i=0; i<min.length; i++){
				if(min[i]>max){
					max=min[i];
					index=i;
				}
			}
			
			//��ʱ�ѵõ��������ʱ������������Դ�ԣ������±�Ϊindex
			submitted.add(index);
			ready[res[index]]=min[index];//���¸���Դ�ľ���ʱ��
			
			gridlet=this.list_.get(index);
			info="��������_"+gridlet.getGridletID();
			resID=resourceID[res[index]];
			System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
			super.gridletSubmit(gridlet, resID);
			
			gridlet=super.gridletReceive();
			System.out.println("���ڽ�����������"+gridlet.getGridletID());
			
			this.receiveList_.add(gridlet);
		}

		/*
		//���ˣ��õ����ֱȽϵ���С�����ʱ�䣬����¼�˶�Ӧ��i��jֵ
		ready[n]=ect[m][n];//���¾���ʱ��
		submitted.add(m);

		gridlet=this.list_.get(m);
		info="��������_"+gridlet.getGridletID();
		resID=resourceID[n];
		System.out.println("����"+info+"��IDΪ"+resID+"����Դ");
		super.gridletSubmit(gridlet, resID);

		gridlet=super.gridletReceive();
		System.out.println("���ڽ�����������"+gridlet.getGridletID());

		this.receiveList_.add(gridlet);*/
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

			/*GridResource resource0=createGridResource("Resource_0", 37);
			GridResource resource1=createGridResource("Resource_1", 39);
			GridResource resource2=createGridResource("Resource_2", 41);*/
			GridResource resource0=createGridResource("Resource_0", 30);
			GridResource resource1=createGridResource("Resource_1", 40);
			GridResource resource2=createGridResource("Resource_2", 50);

			int total_resource=3;//�����洴����Դ�Ĵ������Ӧ

			TestMaxMin user0=new TestMaxMin("User_0", 560.00, total_resource);//���������û���ͬʱ���������񼯺�
			TestMaxMin user1=new TestMaxMin("User_1", 560.00, total_resource);//���������û���ͬʱ���������񼯺�
			TestMaxMin user2=new TestMaxMin("User_2", 560.00, total_resource);//���������û���ͬʱ���������񼯺�
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
		//1.����һ�������б�������洢һ̨���̨����
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
