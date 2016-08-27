package test08;

import java.util.Calendar;
import java.util.LinkedList;

import gridsim.AllocPolicy;
import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;

public class Test8 extends GridSim{

	private Integer ID_;
	private String name_;
	private GridletList list_;
	private GridletList receiveList_;
	private int totalResource_;
	
	/**
	 * ����һ���µ�Test8����
	 * @param name	�ö����ʵ����
	 * @param baudRate	ͨ���ٶ�
	 * @param total_resource	���е�������Դ����
	 * @param numGridlet
	 * @throws Exception	δ��ʼ��GridSim������ʵ����Ϊ�գ������쳣
	 */
	public Test8(String name, double baudRate, int total_resource, int numGridlet) throws Exception {
		super(name, baudRate);
		this.name_=name;
		this.totalResource_=total_resource;
		this.receiveList_=new GridletList();
		
		//Ϊʵ���ȡID
		this.ID_=new Integer(getEntityId(name));
		System.out.println("���ڴ���һ����Ϊ"+name+"��id="+this.ID_+"�������û�ʵ��");
		
		//Ϊ�����û�����һ���������񼯺�
		this.list_=createGridlet(this.ID_.intValue(), numGridlet);
		System.out.println(name+":���ڴ���"+this.list_.size()+"����������");
	}
	
	public void body(){
		int resourceID[]=new int[this.totalResource_];
		double resourceCost[]=new double[this.totalResource_];
		String resourceName[]=new String[this.totalResource_];
		
		LinkedList resList;
		ResourceCharacteristics resChar;
		
		while(true){
			super.gridSimHold(1.0);
			
			resList=getGridResourceList();
			if(resList.size()==this.totalResource_){
				break;
			}else{
				System.out.println(this.name_+":�ȴ���ȡ��Դ�б�...");
			}
		}
		
		//ѭ���Ի�ȡ���п�����Դ
		int i=0;
		for(i=0; i<this.totalResource_;i++){
			resourceID[i]=((Integer)resList.get(i)).intValue();
			
			send(resourceID[i], GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, this.ID_);
			
			resChar=(ResourceCharacteristics)receiveEventObject();
			resourceName[i]=resChar.getResourceName();
			resourceCost[i]=resChar.getCostPerSec();
			
			System.out.println(this.name_+":����Ϊ"+resourceName[i]+"��id="+resourceID[i]+"����Դ�յ���Դ����");
		}
		
        /////////////////////////////////////////////////////
        // SUBMITS Gridlets
		
		Gridlet gridlet=null;
		String info;
		
		int id=0;
		boolean success=false;
		
		for(i=0; i<this.list_.size(); i++){
			gridlet=this.list_.get(i);
			info="����_"+gridlet.getGridletID();
			
			System.out.println(this.name_+":���ڷ���"+info+"����Ϊ"+resourceName[id]+"��id="+resourceID[id]+"��������Դ��ʱ��Ϊ"+GridSim.clock());
			
			if(i%2==0){
				success=gridletSubmit(gridlet, resourceID[id], 0.0, true);
				System.out.println("Ack="+success);
				System.out.println();
			}else{
				success=gridletSubmit(gridlet, resourceID[id], 0.0, false);
			}
		}
		
        //////////////////////////////////////////////////
        // RECEIVES Gridlets
		
		super.gridSimHold(20);
		System.out.println("<<<<<<<<<<��ͣ20��>>>>>>>>>>");
		
		for(i=0; i<this.list_.size(); i++){
			gridlet=(Gridlet) super.receiveEventObject();
			
			System.out.println(this.name_+":���ڽ�����������"+gridlet.getGridletID());
			
			this.receiveList_.add(gridlet);
		}
		
		shutdownUserEntity();
		terminateIOEntities();
		System.out.println(this.name_+":%%%%%%�˳�body()%%%%%%");
	}
	
	/**
	 * ��ȡ���������б�
	 * @return	һ�����������б�
	 */
	public GridletList getGridletList(){
		return this.receiveList_;
	}

	/**
	 * ������������
	 * @param userID	ӵ����Щ����������û�ʵ��ID
	 * @param numGridlet	
	 * @return	һ�����������б����
	 */
	private GridletList createGridlet(int userID, int numGridlet) {
		GridletList list=new GridletList();
		
		int data[]={900, 600, 200, 300, 400, 500, 600};
		int size=0;
		if(numGridlet>=data.length){
			size=6;
		}else{
			size=numGridlet;
		}
		
		for(int i=0; i<size; i++){
			Gridlet gl=new Gridlet(i, data[i], data[i], data[i]);
			gl.setUserID(userID);
			list.add(gl);
		}
		
		return list;
	}
	
	////////////////////////��̬����////////////////////////
	
	public static void main(String[] args) {
		System.out.println("��ʼTest8");
		
		try {
			//First step
			int num_user=1;
			Calendar calendar=Calendar.getInstance();
			boolean trace_flag=true;
			
			String[] exclude_from_file={""};
			String[] exclude_from_processing={""};
			
			String report_name=null;
			
			GridSim.init(num_user, calendar, trace_flag, exclude_from_file, 
					exclude_from_processing, report_name);
			
			//Second step
			NewPolicy test=new NewPolicy("GridResource_0", "NewPolicy");
			GridResource resTest=createGridResource("GridResource_0", test);
			
			//Third step
			int total_resource=1;
			int numGridlet=4;
			double bandwidth=1000.00;
			Test8 user0=new Test8("User_0", bandwidth, total_resource, numGridlet);
			
			//Fourth step
			GridSim.startGridSimulation();
			
			//Final step
			GridletList newList=null;
			newList=user0.getGridletList();
			printGridletList(newList, "User_0");
			System.out.println("Test8������");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("��������");
		}
	}
	
	private static GridResource createGridResource(String name, AllocPolicy obj){
		//1.
		MachineList mList=new MachineList();
		
		//2.
		int mipsRating=377;
		mList.add(new Machine(0, 4, mipsRating));
		
		//3.
		mList.add(new Machine(1, 4, mipsRating));
		mList.add(new Machine(2, 2, mipsRating));
		
		//4.
		String arch="Sun Ultra";
		String os="Solaris";
		double time_zone=9.0;
		double cost=3.0;
		
		ResourceCharacteristics resConfig=new ResourceCharacteristics(
				arch, os, mList, ResourceCharacteristics.SPACE_SHARED, time_zone, cost);
		
		//5.
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
			ResourceCalendar resCalendar=new ResourceCalendar(time_zone, peakLoad, offPeakLoad, holidayLoad, Weekends, Holidays, seed);
			gridRes=new GridResource(name, baud_rate, resConfig, resCalendar, obj);
		} catch (Exception e) {
			System.out.println("msg="+e.getMessage());
		}
		
		System.out.println("����һ����Ϊ"+name+"��������Դ");
		return gridRes;
	}
	
	private static void printGridletList(GridletList list, String name){
		int size=list.size();
		Gridlet gridlet=null;
		
		String indent="	";
		System.out.println();
		System.out.println("==========�û�"+name+"�����==========");
		System.out.println("��������ID"+indent+indent+"״̬"+indent+indent+"��ԴID"+indent+"����");
		
		int i=0;
		for(i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.println(gridlet.getGridletID()+indent+indent+
						gridlet.getGridletStatusString()+indent+indent+
						gridlet.getResourceID()+indent+
						gridlet.getProcessingCost());
		}
		
		for(i=0; i<size; i++){
			gridlet=(Gridlet)list.get(i);
			System.out.println(gridlet.getGridletHistory());
			
			System.out.print("��������#"+gridlet.getGridletID());
			System.out.println("������="+gridlet.getGridletLength()+
						"����ɳ̶�"+gridlet.getGridletFinishedSoFar());
			System.out.println("==============================");
		}
	}

}





















