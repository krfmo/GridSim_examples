package test10;

/*
 * ������������չʾ�����ʹ�û����ġ��Ƚ���Ԥ�����ܡ����磺�������ύ��״̬
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import gridsim.AdvanceReservation;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;


/**
 * һ����ǰԤ����Դ���û�ʵ�塣
 * �ڱ����У�ֻ̽���������ܣ����磺
 * - ����һ���µ���ǰԤ��
 * - ����һ���µļ�ʱԤ������ʱԤ����ζ��ʹ�õ�ǰʱ����Ϊ��ʼʱ��
 * - �ύһ�������ܵĵ�Ԥ��
 * - ���һ��Ԥ����״̬
 */
public class ARTest extends AdvanceReservation{

	private GridletList list_;
	private GridletList receiveList_;
	private int failReservation_;
	
	private final int SEC=1;
	private final int MIN=60*SEC;
	private final int HOUR=60*MIN;
	private final int DAY=24*HOUR;
	
	/**
	 * ����һ�������û�ʵ��
	 * @param name	�ö���ʵ����
	 * @param baudRate	ͨ���ٶ�
	 * @param timeZone	�û�����ʱ��
	 * @param totalJob	��Ҫ��������������������
	 * @throws Exception	��ʼ��ǰ������ʵ���ʵ����Ϊ�������쳣
	 */
	public ARTest(String name, double baudRate, double timeZone,
				int totalJob) throws Exception {
		super(name, baudRate, timeZone);
		this.receiveList_=new GridletList();
		this.failReservation_=0;
		
		//������������
		list_=createGridlet(totalJob, super.get_id());
		
		System.out.println("���ڴ�����Ϊ"+name+"��id="+super.get_id()+"�������û�ʵ��");
		System.out.println(name+":���ڴ���"+totalJob+"����������");
	}
	
	public void body(){
		LinkedList resList;
		
		while(true){
			super.gridSimHold(2*SEC);
			
			resList=getGridResourceList();
			if(resList.size()>0){
				break;
			}else{
				System.out.println(super.get_name()+
						":�ȴ���ȡ��Դ�б�...");
			}
		}
		
		//֧����ǰԤԼ��AR������ԴID����
		ArrayList resARList=new ArrayList();
		
		//֧��AR����Դ���ּ���
		ArrayList resNameList=new ArrayList();
		
		int totalPE=0;
		int i=0;
		Integer intObj=null;//��ԴID
		String name;//��Դ��
		
		//ѭ���Ի�ȡһ�����֧��AR����Դ
		for(i=0; i<resList.size(); i++){
			intObj=(Integer) resList.get(i);
			
			//double checkһ����Դ�Ƿ�֧��AR�������У�������Դ��֧��AR��
			if(GridSim.resourceSupportAR(intObj)==true){
				//�õ���Դ������
				name=GridSim.getEntityName(intObj.intValue());
				
				//�õ���Դӵ�е�PE������
				totalPE=super.getNumPE(intObj);
				
				//����Դ��ID�����ֺ���PE����ӵ����Ǹ��Եļ���
				resARList.add(intObj);
				resNameList.add(name);
			}
		}
		
		//------------------------------------
		//����һ������ԤԼ��һ��������Դʵ��
		sendReservation(resARList, resNameList);
		
		try {
			//Ȼ�����»�ȡ�������������
			int size=list_.size()-failReservation_;
			for(i=0; i<size; i++){
				Gridlet gl=super.gridletReceive();
				this.receiveList_.add(gl);////////////���Ǽ���һ���ˣ�����������ֻ�е�һ��û������...��ô����������أ�����
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//���ֹر�
		shutdownGridStatisticsEntity();
		terminateIOEntities();
		shutdownUserEntity();
		System.out.println(super.get_name()+":%%%%�˳�body()������ʧ�ܵ�ԤԼ����Ϊ"+failReservation_);
	}
	
	/**
	 * ����һ����ԤԼ����������������Դ��
	 * һ��ԤԼֻԤ��1��PE����ʱ��GridSimֻ����һ��PE�ϴ���һ������
	 * @param resARList	Ԥ������ԴID����
	 * @param resNameList	Ԥ������Դ������
	 */
	private void sendReservation(ArrayList resARList, ArrayList resNameList){
		//�ƶ���ԤԼ������1��ԤԼ��Ԥ��һ��PE
		int totalPE=1;
		int totalReservation=list_.size();//������������
		
		//��ȡ����ĳ�ʼʱ��
		Calendar cal=GridSim.getSimulationCalendar();
		
		//��ҪԤԼ�����ʼʱ����һ��
		int MILLI_SEC=1000;
		long time=cal.getTimeInMillis()+(1*DAY*MILLI_SEC);
		
		//ÿ��ԤԼ��Ҫ���10���ӵ�ʱ��
		int duration=10*MIN;
		
		String result=null;
		Gridlet gl=null;
		int val=0;
		int resID=0;//һ����ԴID
		int totalResource=resARList.size();//�ɻ�õ�AR��Դ����
		String resName=null;
		
		Random randObj=new Random(time);
		
		for(int i=0; i<totalReservation; i++){
			duration+=5*MIN;//һ��ԤԼ��ʱ��
			
			//��ȡ��ԴID����Դ��
			val=randObj.nextInt(totalResource);
			resID=((Integer)resARList.get(val)).intValue();
			resName=(String) resNameList.get(val);
			
			//�鿴�Ƿ��Ǽ�ʱԤԼ������ʼʱ��Ϊ0������ζ���Ե�ǰʱ����Ϊ��ʼʱ��
			if(val==i){
				time=0;//��ʹԤԼ
			}else{
				time=cal.getTimeInMillis()+(1*DAY*MILLI_SEC)+(duration*MILLI_SEC);
			}
			
			//����һ���µĻ�ʱԤԼ
			result=super.createReservation(time, duration, totalPE, resID);
			System.out.println(super.get_name()+":����Դ"+resName+"��ԤԼ�����"+result+"��ʱ��Ϊ"+GridSim.clock());
			
			//��ѯ��ЩԤԼ��״̬
			val=super.queryReservation(result);
			System.out.println(super.get_name()+":��ѯ���="+AdvanceReservation.getQueryResult(val));
			
			//���ԤԼʧ�ܣ��������һ����������
			if(val==GridSimTags.AR_STATUS_ERROR||
				val==GridSimTags.AR_STATUS_ERROR_INVALID_BOOKING_ID){
				failReservation_++;
				System.out.println("==================");
				continue;
			}
			
			//����һ����ż����ŵ�ԤԼ��ֱ���ύ��������Ҫ�����κ���������
			if(i%2==0){
				val=super.commitReservation(result);
				System.out.println(super.get_name()+":ֻ�ύ���="+AdvanceReservation.getCommitResult(val));
			}
			
			//һ��ԤԼֻ��ҪԤ��һ��PE
			gl=(Gridlet)list_.get(i);
			val=super.commitReservation(result, gl);
			System.out.println(super.get_name()+":�ύ���="+AdvanceReservation.getCommitResult(val));
			
			//��ѯ���ԤԼ��״̬
			val=super.queryReservation(result);
			System.out.println(super.get_name()+":��ѯ���="+AdvanceReservation.getQueryResult(val));
			System.out.println("========================");
		}
	}
	
	/**
	 * ��ȡһ���������񼯺�
	 * @return	һ���������񼯺�
	 */
	public GridletList getGridletList(){
		return this.receiveList_;
	}

	/**
	 * һ�������û��ж������������Ҫ����
	 * ����������������δ�����������ʹ�û�ʹ��GridSimRandom�ࣩ
	 * @param size	������������
	 * @param userID	�������������������û�
	 * @return	һ�����������б����
	 */
	private GridletList createGridlet(int size, int userID) {
		//����һ���������洢��������
		GridletList list=new GridletList();
		int length=5000;
		for(int i=0; i<size; i++){
			//����һ���µ������������
			Gridlet gridlet=new Gridlet(i, length, 1000, 5000);
			
			//������������뵽�����б�
			list.add(gridlet);
			gridlet.setUserID(userID);
		}
		
		return list;
	}

}














