<?xml version="1.0" encoding="UTF-8"?>
<!--
~ JBoss, Home of Professional Open Source.
~ Copyright 2011, Red Hat, Inc., and individual contributors
~ as indicated by the @author tags. See the copyright.txt file in the
~ distribution for a full listing of individual contributors.
~
~ This is free software; you can redistribute it and/or modify it
~ under the terms of the GNU Lesser General Public License as
~ published by the Free Software Foundation; either version 2.1 of
~ the License, or (at your option) any later version.
~
~ This software is distributed in the hope that it will be useful,
~ but WITHOUT ANY WARRANTY; without even the implied warranty of
~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
~ Lesser General Public License for more details.
~
~ You should have received a copy of the GNU Lesser General Public
~ License along with this software; if not, write to the Free
~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
-->
<xsl:stylesheet version="2.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:xs="http://www.w3.org/2001/XMLSchema"
            xmlns:fn="http://www.w3.org/2005/xpath-functions"
            xmlns:xdt="http://www.w3.org/2005/xpath-datatypes"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:jb="urn:jboss:domain:1.4"
            exclude-result-prefixes="xs xsl xsi fn xdt jb cache-container">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()[name(.)='extensions']">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
         <extension module="org.jboss.as.messaging"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()|@*" name="identity">
       <xsl:copy>
           <xsl:apply-templates select="node()|@*"/>
       </xsl:copy>
   </xsl:template>

   <xsl:template match="*[local-name(.) = 'session-bean']">
       <xsl:copy>
           <xsl:apply-templates select="node()|@*"/>
       </xsl:copy>
       <mdb>
           <resource-adapter-ref resource-adapter-name="hornetq-ra"/>
           <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
       </mdb>

   </xsl:template>

   <xsl:template match="node()[name(.)='profile']">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
         <subsystem xmlns="urn:jboss:domain:messaging:1.4">
            <hornetq-server>
               <persistence-enabled>true</persistence-enabled>
               <journal-type>NIO</journal-type>
               <journal-min-files>2</journal-min-files>
               <connectors>
                  <netty-connector name="netty" socket-binding="messaging"/>
                  <netty-connector name="netty-throughput" socket-binding="messaging-throughput">
                     <param key="batch-delay" value="50"/>
                  </netty-connector>
                  <in-vm-connector name="in-vm" server-id="0"/>
               </connectors>
               <acceptors>
                  <netty-acceptor name="netty" socket-binding="messaging"/>
                  <netty-acceptor name="netty-throughput" socket-binding="messaging-throughput">
                     <param key="batch-delay" value="50"/>
                     <param key="direct-deliver" value="false"/>
                  </netty-acceptor>
                  <in-vm-acceptor name="in-vm" server-id="0"/>
               </acceptors>
               <security-settings>
                  <security-setting match="#">
                     <permission type="send" roles="guest"/>
                     <permission type="consume" roles="guest"/>
                     <permission type="createNonDurableQueue" roles="guest"/>
                     <permission type="deleteNonDurableQueue" roles="guest"/>
                  </security-setting>
               </security-settings>
               <address-settings>
                  <!--default for catch all-->
                  <address-setting match="#">
                     <dead-letter-address>jms.queue.DLQ</dead-letter-address>
                     <expiry-address>jms.queue.ExpiryQueue</expiry-address>
                     <redelivery-delay>0</redelivery-delay>
                     <max-size-bytes>10485760</max-size-bytes>
                     <address-full-policy>BLOCK</address-full-policy>
                     <message-counter-history-day-limit>10</message-counter-history-day-limit>
                  </address-setting>
               </address-settings>
               <jms-connection-factories>
                  <connection-factory name="InVmConnectionFactory">
                     <connectors>
                        <connector-ref connector-name="in-vm"/>
                     </connectors>
                     <entries>
                        <entry name="java:/ConnectionFactory"/>
                     </entries>
                  </connection-factory>
                  <connection-factory name="RemoteConnectionFactory">
                     <connectors>
                        <connector-ref connector-name="netty"/>
                     </connectors>
                     <entries>
                        <entry name="java:jboss/exported/jms/RemoteConnectionFactory"/>
                     </entries>
                  </connection-factory>
                  <pooled-connection-factory name="hornetq-ra">
                     <transaction mode="xa"/>
                     <connectors>
                        <connector-ref connector-name="in-vm"/>
                     </connectors>
                     <entries>
                        <entry name="java:/JmsXA"/>
                     </entries>
                  </pooled-connection-factory>
               </jms-connection-factories>
            </hornetq-server>
         </subsystem>
      </xsl:copy>
   </xsl:template>


   <xsl:template match="node()[name(.)='socket-binding-group']">
       <xsl:copy>
           <xsl:copy-of select="@*"/>
           <socket-binding name="messaging" port="5445"/>
           <socket-binding name="messaging-group" port="0" multicast-address="${{jboss.messaging.group.address:231.7.7.7}}" multicast-port="${{jboss.messaging.group.port:9876}}"/>
           <socket-binding name="messaging-throughput" port="5455"/>
           <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
