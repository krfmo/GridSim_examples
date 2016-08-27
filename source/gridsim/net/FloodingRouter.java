/*
 * ** Network and Service Differentiation Extensions to GridSim 3.0 **
 *
 * Gokul Poduval & Chen-Khong Tham
 * Computer Communication Networks (CCN) Lab
 * Dept of Electrical & Computer Engineering
 * National University of Singapore
 * August 2004
 *
 * License: GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2004, The University of Melbourne, Australia and National
 * University of Singapore
 * FloodingRouter.java - Simulates a network router with Flooding as
 * the advertising protocol
 *
 */
package gridsim.net;

import eduni.simjava.*;
import gridsim.*;
import gridsim.util.*;
import java.util.*;


/**
 * This class implements a Router using a form of Flooding for routing.
 * The routing protocol is run before Gridlets etc. can be
 * submitted.
 * <p>
 * In case there are more than two routes to a destination, the
 * route with the lower hopcount is used. Since in this simulation routers
 * relay perfect information and links do not break down, flooding should be a
 * reliable protocol to use.
 *
 * @invariant $none
 * @since GridSim Toolkit 3.1
 * @author Gokul Poduval & Chen-Khong Tham, National University of Singapore
 */
public class FloodingRouter extends Router
{
    private Hashtable linkTable;
    private Hashtable schedTable; // table of schedulers
    private Hashtable hostTable;
    private Hashtable routerTable;
    private Hashtable forwardTable;
    private int id;


    /**
     * Creates a new FloodingRouter object. By default,
     * <b>no recording or logging</b>
     * is done for packets' activities. If you want to log operations of this
     * entity, please use {@link #FloodingRouter(String, boolean)}.
     *
     * @param name Name of this router
     * @throws NullPointerException This happens when name is empty or null
     * @see #FloodingRouter(String, boolean)
     * @pre name != null
     * @post $none
     */
    public FloodingRouter(String name) throws NullPointerException {
        this(name, false);
    }

    /**
     * Creates a new FloodingRouter object with logging facility if it
     * is turned on.
     * <br>
     * NOTE: If logging facility is turned on, there are some overheads
     * in terms of performance and memory consumption.
     *
     * @param name      Name of this router
     * @param trace     <tt>true</tt> if you want to record this router's
     *                  activity, <tt>false</tt> otherwise
     * @throws NullPointerException This happens when name is empty or null
     * @pre name != null
     * @post $none
     */
    public FloodingRouter(String name, boolean trace) throws
                          NullPointerException
    {
        super(name, trace);
        init();
    }

    /**
     * Initialises all variables
     * @pre $none
     * @post $none
     */
    private void init()
    {
        this.id = super.get_id();
        linkTable = new Hashtable();
        hostTable = new Hashtable();
        routerTable = new Hashtable();
        forwardTable = new Hashtable();
        schedTable = new Hashtable();
    }

    /**
     * Informs the registered entities regarding to the end of a simulation.
     * @pre $none
     * @post $none
     */
    protected void processEndSimulation() {
        // ... empty - maybe need to clean up all the hashtables
    }

    /**
     * Joins two routers with a Link.
     * @param router    The router on the other side to which this one will
     *                  be attached.
     * @param link      This is the link that will be used to connect the two
     *                  routers.
     * @param thisSched The scheduling policy used on this routers egress port
     *                  when sending data through it.
     * @param otherSched    The scheduling policy that will be used on the
     *                      egress port of the router being connected to when
     *                      sending data to this router.
     * @pre router != null
     * @pre link != null
     * @pre thisSched != null
     * @pre otherSched != null
     * @post $none
     */
    public void attachRouter(Router router, Link link,
                    PacketScheduler thisSched, PacketScheduler otherSched)
    {
        String msg = super.get_name() + ".attachRouter(): Error - ";
        if (router == null)
        {
            System.out.println(msg + "the router is null.");
            return;
        }

        if (link == null)
        {
            System.out.println(msg + "the link is null.");
            return;
        }

        if (thisSched == null || otherSched == null)
        {
            System.out.println(msg +
                    "the one or more packet schedulers are null.");
            return;
        }

        thisSched.setBaudRate( link.getBaudRate() );
        otherSched.setBaudRate( link.getBaudRate() );

        link.attach(this, router);
        this.attachRouter(router, link, thisSched);
        router.attachRouter(this, link, otherSched);
    }

    /**
     * Joins two routers together. This is called by the routers themselves
     * and should not be called by other entities.
     *
     * @param router    The Router to which this router will be connected.
     * @param link      The Link that will be used to join these routers.
     * @param sched     The scheduling policy used on the egress port of the
     *                  router when sending data through this route.
     * @pre router != null
     * @pre link != null
     * @pre sched != null
     * @post $none
     */
    public void attachRouter(Router router, Link link, PacketScheduler sched)
    {
        String msg = super.get_name() + ".attachRouter(): Error - ";
        if (router == null)
        {
            System.out.println(msg + "the router is null.");
            return;
        }

        if (link == null)
        {
            System.out.println(msg + "the link is null.");
            return;
        }

        if (sched == null)
        {
            System.out.println(msg + "the packet scheduler is null.");
            return;
        }

        linkTable.put(router.get_name(), link.get_name());

        if (!schedTable.containsKey( link.get_name()) ) {
            schedTable.put(link.get_name(), sched);
        }

        routerTable.put( link.get_name(), router.get_name() );
        hostTable.put( link.get_name(), router.get_name() );

        // logging or recording ...
        if (reportWriter_ != null)
        {
            StringBuffer sb = null;
            sb = new StringBuffer("attach this ROUTER, with router, ");
            sb.append( router.get_name() );
            sb.append(", with link, ");
            sb.append( link.get_name() );
            sb.append(", with packet scheduler, ");
            sb.append( sched.getSchedName() );

            super.write( sb.toString() );
        }
    }

    /**
     * Attaches an entity to this router. The link between the router and the
     * entity being attached is taken from
     * {@link gridsim.GridSimCore#getLink()}.
     *
     * @param entity    The entity to be attached.
     * @param sched     The scheduling policy that will be used on the egress
     *                  port when the router sends data to the entity being
     *                  joined.
     * @see gridsim.GridSimCore#getLink()
     * @pre entity != null
     * @pre sched != null
     * @post $none
     */
    public void attachHost(GridSimCore entity, PacketScheduler sched)
    {
        String msg = super.get_name() + ".attachHost(): Error - ";
        if (entity == null)
        {
            System.out.println(msg + "the entity is null.");
            return;
        }

        if (sched == null)
        {
            System.out.println(msg + "the packet scheduler is null.");
            return;
        }

        Link link = entity.getLink();
        sched.setBaudRate( link.getBaudRate() );
        link.attach(this, entity);
        linkTable.put( entity.get_name(), link.get_name() );

        if (!schedTable.containsKey( link.get_name() )) {
            schedTable.put(link.get_name(), sched);
        }

        hostTable.put( link.get_name(), entity.get_name() );

        // recording ...
        if (reportWriter_ != null)
        {
            StringBuffer sb = null;
            sb = new StringBuffer("attach this ROUTER, to entity, ");
            sb.append( entity.get_name() );
            sb.append(", with packet scheduler, ");
            sb.append( sched.getSchedName() );

            super.write( sb.toString() );
        }
    }

    /**
     * Processes incoming events
     * @param ev    a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected synchronized void processEvent(Sim_event ev)
    {
        switch ( ev.get_tag() )
        {
            case GridSimTags.PKT_FORWARD:
            case GridSimTags.JUNK_PKT:
                processNetPacket(ev, ev.get_tag());
                break;

            case GridSimTags.ROUTER_AD:
                receiveAd(ev);
                break;

            case GridSimTags.INSIGNIFICANT:
                processInternalEvent(ev);
                break;

            default:
                System.out.println(super.get_name() + ".body(): Unable to " +
                        "handle request from GridSimTags " +
                        "with constant number " + ev.get_tag() );
                break;
        }
    }

    /**
     * Processes incoming network packets, one at a time.
     * The incoming packet will be split up into smaller pieces if
     * the packet size > MTU of the other end.
     *
     * @param ev    a Sim_event object
     * @pre ev != null
     * @post $none
     */
    private synchronized void processNetPacket(Sim_event ev, int tag)
    {
        double nextTime = 0;
        Packet pkt = (Packet) ev.get_data();
        PacketScheduler sched = getScheduler(pkt);

        // if a packet scheduler is not found, then try reschedule this packet
        // in the future
        if (sched == null)
        {
            System.out.println(super.get_name() + ".processNetPacket(): " +
                "Warning - can't find a packet scheduler for " + pkt);
            System.out.println("-> Will reschedule it again in the future.");

            super.sim_schedule(super.get_id(), Router.DELAY, tag, pkt);
            return;
        }

        // process ping() request
        if (pkt instanceof InfoPacket)
        {
            ((InfoPacket) pkt).addHop(id);
            ((InfoPacket) pkt).addEntryTime( GridSim.clock() );
            ((InfoPacket) pkt).addBaudRate(sched.getBaudRate());
        }

        // check downlink MTU, and split accordingly
        String linkName = getLinkName( pkt.getDestID() );
        Link downLink = (Link) Sim_system.get_entity(linkName);
        int MTU = downLink.getMTU();
        int numPackets = (int) Math.ceil(pkt.getSize() / (MTU * 1.0));

        // if no packets at the moment
        if (sched.size() == 0)
        {
            if (numPackets == 1) {
                nextTime = (pkt.getSize() * NetIO.BITS) / sched.getBaudRate();
            }
            else {
                nextTime = (MTU * NetIO.BITS * 1.0) / sched.getBaudRate();
            }

            sendInternalEvent(nextTime, sched);
        }

        // log / record ....
        if (super.reportWriter_ != null)
        {
            super.write("");
            super.write("receive incoming, " + pkt + ", delay, " + nextTime);
            super.write("break this packet into, " + numPackets);
        }

        // break a large packet into smaller ones that fit into MTU
        // by making null or empty packets except for the last one
        for (int i = 0; i < numPackets - 1; i++)
        {
            NetPacket np = new NetPacket(null, pkt.getID(), MTU, tag,
                                     pkt.getSrcID(), pkt.getDestID(),
                                     pkt.getNetServiceType(), i+1, numPackets);

            np.setLast(id);
            if (super.reportWriter_ != null) {
                super.write("enqueing, " + np); 
            }
            sched.enque(np);  // put the packet into the scheduler
        }

        // put the actual packet into the last one and resize it accordingly
        pkt.setLast(id);
        pkt.setSize(pkt.getSize() - MTU * (numPackets - 1));
        if (super.reportWriter_ != null) {
            super.write("enqueing, " + pkt);
        }
        sched.enque(pkt);  // put the packet into the scheduler
    }

    /**
     * Gets the link's name for a given id
     * @param destID    a destination id
     * @return the link's man
     * @pre destID > 0
     * @post $none
     */
    private synchronized String getLinkName(int destID)
    {
        String destName = GridSim.getEntityName(destID);
        String linkName = null;

        //directly connected
        if (hostTable.containsValue(destName)) {
            linkName = (String)linkTable.get(destName);
        }
        else
        {
            // need to forward to another router
            Object[] data = (Object[]) forwardTable.get(destName);
            String router = (String) data[0];
            linkName = (String) linkTable.get(router);
        }

        return linkName;
    }

    /**
     * Returns the Scheduler associated with a packet.
     *
     * @param np NetPacket for which the associated scheduler is to be returned
     * @return the packet's scheduler or <tt>null</tt> if the packet is empty
     * @pre np != null
     * @post $none
     */
    public PacketScheduler getScheduler(Packet np)
    {
        if (np == null) {
            return null;
        }

        String destName = GridSim.getEntityName( np.getDestID() );
        return getScheduler(destName);
    }

    /**
     * Returns the Scheduler that the router would use to reach a particular
     * destination. This can be used to set weigths, priorities etc. as the
     * case may be on the Scheduler
     *
     * @param dest id of the destination for which the Scheduler is required.
     * @return the destination's packet scheduler
     * @pre dest > 0
     * @post $none
     */
    public PacketScheduler getScheduler(int dest)
    {
        if (dest < 0) {
            return null;
        }

        String destName = GridSim.getEntityName(dest);
        return getScheduler(destName);
    }

    /**
     * Returns the Scheduler that the router would use to reach a particular
     * destination. This can be used to set weigths, priorities etc. as the
     * case may be on the Scheduler
     *
     * @param dest Name of the destination for which the Scheduler is required.
     * @return destination's packet scheduler or <tt>null</tt> if destination
     *         name is invalid.
     * @pre dest != null
     * @post $none
     */
    public PacketScheduler getScheduler(String dest)
    {
        if (dest == null || dest.length() == 0) {
            return null;
        }

        PacketScheduler sched = null;
        try
        {
            if ( hostTable.containsValue(dest) )
            {
                String linkName = (String) linkTable.get(dest);
                sched = (PacketScheduler) schedTable.get(linkName);
            }
            else
            {
                // need to forward to another router
                Object[] data = (Object[]) forwardTable.get(dest);

                // in case the forwarding table is incomplete
                if (data == null) {
                    return null;
                }

                String router = (String) data[0];
                String linkName = (String) linkTable.get(router);
                sched = (PacketScheduler) schedTable.get(linkName);
            }
        }
        catch (Exception e) {
            sched = null;
        }

        return sched;
    }

    /**
     * Processes internal event
     * @param ev    a Sim_event object
     * @pre ev != null
     * @post $none
     */
    private synchronized void processInternalEvent(Sim_event ev)
    {
        PacketScheduler sched = (PacketScheduler) ev.get_data();
        dequeue(sched);
    }

    /**
     * Dequeue a packet from the scheduler and sends it to the next
     * destination via a link.
     * @param sched  the packet scheduler
     * @pre sched != null
     * @post $none
     */
    private synchronized void dequeue(PacketScheduler sched)
    {
        Packet np = sched.deque();

        // process ping() packet
        if (np instanceof InfoPacket) {
            ((InfoPacket) np).addExitTime( GridSim.clock() );
        }

        if (super.reportWriter_ != null) {
            super.write("dequeuing, " + np); 
        }

        // must distinguish between normal and junk packet
        int tag = GridSimTags.PKT_FORWARD;
        if (np.getTag() == GridSimTags.JUNK_PKT) {
            tag = GridSimTags.JUNK_PKT;
        }

        // sends the packet via the link
        String linkName = getLinkName( np.getDestID() );
        super.sim_schedule(GridSim.getEntityId(linkName),
                           GridSimTags.SCHEDULE_NOW, tag, np);

        // process the next packet in the scheduler
        if ( !sched.isEmpty() )
        {
            double nextTime = (np.getSize() * NetIO.BITS) / sched.getBaudRate();
            sendInternalEvent(nextTime, sched);
        }
    }

    /**
     * Sends an internal event to itself
     * @param time  the delay time period
     * @param data  the data to be sent
     * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
     * @pre time >= 0
     * @post $none
     */
    private synchronized boolean sendInternalEvent(double time, Object data)
    {
        if (time < 0.0) {
            return false;
        }

        super.sim_schedule(id, time, GridSimTags.INSIGNIFICANT, data);
        return true;
    }

    /**
     * Prints this router's routing table in a nice-formatted layout
     * @pre $none
     * @post $none
     */
    public synchronized void printRoutingTable()
    {
        synchronized (System.out)
        {
            System.out.println();
            System.out.println("--- Routing Table for " +
                               super.get_name() + " ---");

            for (Enumeration e = hostTable.keys(); e.hasMoreElements(); )
            {
                String link = (String) e.nextElement();
                System.out.println(hostTable.get(link) + "\t\t" + link);
            }

            for (Enumeration e = forwardTable.keys(); e.hasMoreElements(); )
            {
                String host = (String)e.nextElement();
                Object[] data = (Object[])forwardTable.get(host);
                String nextHop = (String)data[0];
                System.out.println(host + "\t\t" + nextHop);
            }

            System.out.println("-------------------------------------");
            System.out.println();
        }
    }


    //----------- ADVERTISING FUNCTIONS --------------//

    /**
     * All hosts connected to this router are advertised to adjacent routers
     * @pre $none
     * @post $none
     */
    protected synchronized void advertiseHosts()
    {
        Collection hosts = hostTable.values(); // who to advertise
        Enumeration routers = routerTable.elements();

        while ( routers.hasMoreElements() )
        {
            FloodAdPack ad = new FloodAdPack(super.get_name(), hosts);
            String router = (String) routers.nextElement();
            if (super.reportWriter_ != null) {
                super.write("advertise to router, " + router);
            }
            sim_schedule(Sim_system.get_entity_id(router),
                         GridSimTags.SCHEDULE_NOW, GridSimTags.ROUTER_AD, ad);
        }

        super.sim_pause(5);   // wait for 5 secs to gather the results
    }

    /**
     * When an ad is recieved, the forwarding table is updated. After that we
     * need to propogate this ad along all links except the incoming one.
     * {@link #forwardAd(FloodAdPack)} is used for that
     * @param ev  a Sim_event object
     * @pre ev != null
     * @post $none
     */
    private synchronized void receiveAd(Sim_event ev)
    {
        if (super.reportWriter_ != null) {
            super.write("receive router ad from, " +
                    GridSim.getEntityName(ev.get_src()) );
        }
        
        // what to do when an ad is received
        FloodAdPack ad = (FloodAdPack)ev.get_data();

        // prevent count-to-infinity
        if (ad.getHopCount() > Router.MAX_HOP_COUNT) {
            return;
        }

        String sender = ad.getSender();
        Iterator it = ad.getHosts().iterator();

        while ( it.hasNext() )
        {
            String host = (String) it.next();

            if ( host.equals(super.get_name()) ) {
                continue;
            }

            if (hostTable.containsValue(host)) { // direct connection
                continue;
            }

            if (forwardTable.containsKey(host))
            {
                Object[] data = (Object[])forwardTable.get(host);
                int hop = ((Integer)data[1]).intValue();

                if ((hop) > ad.getHopCount())
                {
                    Object[] toPut = { sender, new Integer(ad.getHopCount()) };
                    forwardTable.put(host, toPut);
                }
            }
            else
            {
                Object[] toPut = { sender, new Integer(ad.getHopCount()) };
                forwardTable.put(host, toPut);
            }
        }

        forwardAd(ad);
    }

    /**
     * Received ads should be forwarded along all links except the incoming
     * one. Also need to change id to onself
     *
     * @param ad    a FloodAdPack object
     * @pre ad != null
     * @post $none
     */
    private synchronized void forwardAd(FloodAdPack ad)
    {
        String sender = ad.getSender();
        ad.incrementHopCount();
        FloodAdPack newad = new FloodAdPack(super.get_name(),ad.getHosts());
        newad.setHopCount(ad.getHopCount());

        Enumeration routers = routerTable.elements();
        while ( routers.hasMoreElements() )
        {
            String router = (String)routers.nextElement();
            if (!router.equals(sender))
            {
                sim_schedule(Sim_system.get_entity_id(router),
                      GridSimTags.SCHEDULE_NOW, GridSimTags.ROUTER_AD, newad);
            }
        }
    }

} // end class

