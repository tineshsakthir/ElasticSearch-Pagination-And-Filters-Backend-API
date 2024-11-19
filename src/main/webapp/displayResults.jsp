<%--
  Created by IntelliJ IDEA.
  User: tines
  Date: 14-Nov-2024
  Time: 12:18 am
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="jakarta.servlet.http.HttpServletRequest" %>


<%
      // Retrieving the parameters from the request
      String[] senders = request.getParameterValues("SENDER[]");
      String[] recipients = request.getParameterValues("RECIPIENT[]");
      String[] messageTraceIds = request.getParameterValues("MESSAGE_TRACE_ID[]");
      String[] subjects = request.getParameterValues("SUBJECT[]");
      String[] fromIps = request.getParameterValues("FROM_IP[]");
      String[] toIps = request.getParameterValues("TO_IP[]");
      String[] sizes = request.getParameterValues("SIZE[]");
      String[] sizes_from = request.getParameterValues("SIZE_FROM[]");
      String[] sizes_to = request.getParameterValues("SIZE_TO[]");
      String[] receiveds = request.getParameterValues("RECEIVED[]");
      String[] receiveds_from = request.getParameterValues("RECEIVED_FROM[]");
      String[] receiveds_to = request.getParameterValues("RECEIVED_TO[]");

      // Printing the arrays using Arrays.toString()
%>

<!DOCTYPE html>
<html>
<head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Message Trace Results</title>

      <%--    bootstrap css start --%>
      <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
      <%--    bootstrap css end --%>

      <%--    custom css start --%>
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/displayTable.css">
      <link rel="stylesheet" href="${pageContext.request.contextPath}/css/filterComponentStyling.css">
      <%--    custom css end --%>

      <style>

      </style>

</head>
<body>

<h1>Submitted Filter Parameters</h1>

<p><strong>Senders:</strong> <%= Arrays.toString(senders) %>
</p>
<p><strong>Recipients:</strong> <%= Arrays.toString(recipients) %>
</p>
<p><strong>Message Trace Id:</strong> <%= Arrays.toString(messageTraceIds) %>
</p>
<p><strong>Subjects:</strong> <%= Arrays.toString(subjects) %>
</p>
<p><strong>From IP:</strong> <%= Arrays.toString(fromIps) %>
</p>
<p><strong>To IP:</strong> <%= Arrays.toString(toIps) %>
</p>
<p><strong>Sizes:</strong> <%= Arrays.toString(sizes) %>
</p>
<p><strong>Sizes From:</strong> <%= Arrays.toString(sizes_from) %>
</p>
<p><strong>Sizes To:</strong> <%= Arrays.toString(sizes_to) %>
</p>
<p><strong>Received:</strong> <%= Arrays.toString(receiveds) %>
</p>
<p><strong>Received From:</strong> <%= Arrays.toString(receiveds_from) %>
</p>
<p><strong>Received To:</strong> <%= Arrays.toString(receiveds_to) %>
</p>


<!-- Add Filter Button -->
<button type="button" class="btn btn-primary" onclick="showFilterOptions()">Add Filter</button>

<!-- Filter Options -->
<div id="filter-buttons" class="filter-buttons">
      <%--    <button id="SenderEmail" type="button" class="btn btn-outline-secondary" onclick="addFilterField('SenderEmail')">Sender Email</button>--%>
      <%--    <button id="ReceiverEmail" type="button" class="btn btn-outline-secondary" onclick="addFilterField('ReceiverEmail')">Receiver Email</button>--%>
      <%--    <button id="MessageTrace" type="button" class="btn btn-outline-secondary" onclick="addFilterField('MessageTrace')">Message Trace</button>--%>
      <%--    <button id="Subject" type="button" class="btn btn-outline-secondary" onclick="addFilterField('Subject')">Subject</button>--%>
      <%--    <button id="FromIp" type="button" class="btn btn-outline-secondary" onclick="addFilterField('FromIp')">From IP</button>--%>
      <%--    <button id="ToIp" type="button" class="btn btn-outline-secondary" onclick="addFilterField('ToIp')">To IP</button>--%>
      <%--    <button id="MessageSize" type="button" class="btn btn-outline-secondary" onclick="addFilterField('MessageSize')">Message Size</button>--%>
      <%--    <button id="DateRange" type="button" class="btn btn-outline-secondary" onclick="addFilterField('DateRange')">Date Range</button>--%>


      <button type="button" class="btn btn-outline-secondary" onclick="addSenderField()">Add Sender</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addRecipientField()">Add Recipient</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addMessageTraceIdField()">Add Message Trace ID
      </button>
      <button type="button" class="btn btn-outline-secondary" onclick="addSubjectField()">Add Subject</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addFromIpField()">Add From IP</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addToIpField()">Add To IP</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addSizeField()">Add Size</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addSizeRangeField()">Add Size Range</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addReceivedField()">Add Received Date</button>
      <button type="button" class="btn btn-outline-secondary" onclick="addReceivedRangeField()">Add Received Date Range
      </button>
</div>

<!-- Filter Fields Container -->
<%-- Don't need to pass the page number for this form, by default picks the page number 1 in the pagination servlet--%>
<form id="filter-container" class="filter-container" action="${pageContext.request.contextPath}/pagination"
      method="get">
      <input type="hidden" name="action" value="filter">

      <div id="SENDERS">
            <%
                  if (senders != null) {
                        for (String sender : senders) {
            %>
            <div class="filter-field">
                  <label>Sender Email</label>
                  <input type="email" name="SENDER[]" class="form-control" placeholder="Enter Sender Email"
                         value="<%= sender %>" required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="RECIPIENTS">
            <%
                  if (recipients != null) {
                        for (String recipient : recipients) {
            %>
            <div class="filter-field">
                  <label>Recipient Email</label>
                  <input type="email" name="RECIPIENT[]" class="form-control" placeholder="Enter Recipient Email"
                         value="<%= recipient %>" required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="MESSAGE_TRACE_IDS">
            <%
                  if (messageTraceIds != null) {
                        for (String messageTraceId : messageTraceIds) {
            %>
            <div class="filter-field">
                  <label>Message Trace ID</label>
                  <input type="text" name="MESSAGE_TRACE_ID[]" class="form-control" placeholder="Enter Message Trace ID"
                         value="<%= messageTraceId %>" required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="SUBJECTS">
            <%
                  if (subjects != null) {
                        for (String subject : subjects) {
            %>
            <div class="filter-field">
                  <label>Subject</label>
                  <input type="text" name="SUBJECT[]" class="form-control" placeholder="Enter Subject"
                         value="<%= subject %>"
                         required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="FROM_IPS">
            <%
                  if (fromIps != null) {
                        for (String fromIp : fromIps) {
            %>
            <div class="filter-field">
                  <label>From IP</label>
                  <input type="text" name="FROM_IP[]" class="form-control" placeholder="Enter From IP"
                         value="<%= fromIp %>"
                         required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="TO_IPS">
            <%
                  if (toIps != null) {
                        for (String toIp : toIps) {
            %>
            <div class="filter-field">
                  <label>To IP</label>
                  <input type="text" name="TO_IP[]" class="form-control" placeholder="Enter To IP" value="<%= toIp
             %>" required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="SIZES">
            <%
                  if (sizes != null) {
                        for (String size : sizes) {
            %>
            <div class="filter-field">
                  <label>Message Size</label>
                  <input type="number" name="SIZE[]" class="form-control" placeholder="Enter Size" value="<%= size %>"
                         required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="SIZE_RANGES">
            <%
                  if (sizes_from != null && sizes_to != null && sizes_from.length == sizes_to.length) {
                        for (int i = 0; i < sizes_from.length; i++) {
            %>
            <div class="filter-field">
                  <label>Message Size Range</label>
                  <input type="number" name="SIZE_FROM[]" class="form-control" placeholder="Min Size"
                         value="<%= sizes_from[i] %>" required>
                  <input type="number" name="SIZE_TO[]" class="form-control" placeholder="Max Size"
                         value="<%= sizes_to[i] %>"
                         required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>

      <div id="RECEIVEDS">
            <%
                  if (receiveds != null) {
                        for (String received : receiveds) {
            %>
            <div class="filter-field">
                  <label>Received Date</label>
                  <input type="datetime-local" name="RECEIVED[]" class="form-control" placeholder="Enter Date"
                         value="<%=received%>" required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>


      <div id="RECEIVED_RANGES">
            <%
                  if (receiveds_from != null && receiveds_to != null && receiveds_from.length == receiveds_to.length) {
                        for (int i = 0; i < receiveds_from.length; i++) {
            %>
            <div class="filter-field">
                  <label>Received Date Range</label>
                  <input type="datetime-local" name="RECEIVED_FROM[]" class="form-control" placeholder="From Date"
                         value="<%=receiveds_from[i]%>" required>
                  <input type="datetime-local" name="RECEIVED_TO[]" class="form-control" placeholder="To Date"
                         value="<%=receiveds_to[i]%>" required>
                  <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>
            <%
                        }
                  }
            %>
      </div>
      <button type="submit">Apply Filters</button>
</form>

<%----%>
<%----%>
<%----%>
<%--The above is developed, styled and scripted separately--%>
<%----%>
<%----%>
<%----%>


<%-- Start : Bean definition for Next/Previous page features and Table content--%>
<jsp:useBean id="mailTraces" scope="request" type="java.util.List"/>
<jsp:useBean id="currentPage" scope="request" type="java.lang.Integer"/>
<jsp:useBean id="hasNextPage" scope="request" type="java.lang.Boolean"/>
<%-- End : Bean definition for Next/Previous page features and Table content--%>

<%-- Start : Next page and previous page features :) starts --%>
<div class="pagination-container">
      <c:if test="${currentPage > 1}">
            <form action="${pageContext.request.contextPath}/pagination" method="post">
                  <input type="hidden" name="action" value="pagination">
                        <%--Sender Start--%>
                  <%
                        if (senders != null) {
                              for (String sender : senders) {
                  %>
                  <input type="hidden" name="SENDER[]" class="form-control" placeholder="Enter Sender Email"
                         value="<%= sender %>" required>
                  <%
                              }
                        }
                  %>
                        <%--Sender Start--%>

                        <%--Recipient Start--%>
                  <%
                        if (recipients != null) {
                              for (String recipient : recipients) {
                  %>
                  <input type="hidden" name="RECIPIENT[]" class="form-control" placeholder="Enter Recipient Email"
                         value="<%= recipient %>" required>
                  <%
                              }
                        }
                  %>
                        <%--Recipient End--%>

                        <%--MessageTraceId Start--%>
                  <%
                        if (messageTraceIds != null) {
                              for (String messageTraceId : messageTraceIds) {
                  %>
                  <input type="hidden" name="MESSAGE_TRACE_ID[]" class="form-control" placeholder="Enter Message Trace ID"
                         value="<%= messageTraceId %>" required>

                  <%
                              }
                        }
                  %>
                        <%--MessageTraceId End--%>

                        <%--Subject start--%>
                  <%
                        if (subjects != null) {
                              for (String subject : subjects) {
                  %>

                  <input type="hidden" name="SUBJECT[]" class="form-control" placeholder="Enter Subject"
                         value="<%= subject %>"
                         required>

                  <%
                              }
                        }
                  %>
                        <%--Subject End--%>

                        <%--From Ip Start--%>
                  <%
                        if (fromIps != null) {
                              for (String fromIp : fromIps) {
                  %>
                  <input type="hidden" name="FROM_IP[]" class="form-control" placeholder="Enter From IP"
                         value="<%= fromIp %>"
                         required>
                  <%
                              }
                        }
                  %>
                        <%--From IP End--%>

                        <%--To IP Start--%>
                  <%
                        if (toIps != null) {
                              for (String toIp : toIps) {
                  %>
                  <input type="hidden" name="TO_IP[]" class="form-control" placeholder="Enter To IP" value="<%= toIp
             %>" required>
                  <%
                              }
                        }
                  %>
                        <%--To IP End--%>

                        <%--Sizes Start--%>
                  <%
                        if (sizes != null) {
                              for (String size : sizes) {
                  %>

                  <input type="hidden" name="SIZE[]" class="form-control" placeholder="Enter Size" value="<%= size %>"
                         required>
                  <%
                              }
                        }
                  %>
                        <%--Sizes End--%>

                        <%--Sizes_from and Sizes_to Start--%>
                  <%
                        if (sizes_from != null && sizes_to != null && sizes_from.length == sizes_to.length) {
                              for (int i = 0; i < sizes_from.length; i++) {
                  %>

                  <input type="hidden" name="SIZE_FROM[]" class="form-control" placeholder="Min Size"
                         value="<%= sizes_from[i] %>" required>
                  <input type="hidden" name="SIZE_TO[]" class="form-control" placeholder="Max Size"
                         value="<%= sizes_to[i] %>"
                         required>

                  <%
                              }
                        }
                  %>
                        <%--Sizes_from and Sizes_to End--%>

                        <%--Receiveds Start--%>
                  <%
                        if (receiveds != null) {
                              for (String received : receiveds) {
                  %>

                  <input type="hidden" name="RECEIVED[]" class="form-control" placeholder="Enter Date"
                         value="<%=received%>" required>

                  <%
                              }
                        }
                  %>
                        <%--Received End--%>

                        <%--Received_from and Received_to Start--%>
                  <%
                        if (receiveds_from != null && receiveds_to != null && receiveds_from.length == receiveds_to.length) {
                              for (int i = 0; i < receiveds_from.length; i++) {
                  %>

                  <input type="hidden" name="RECEIVED_FROM[]" class="form-control" placeholder="From Date"
                         value="<%=receiveds_from[i]%>" required>
                  <input type="hidden" name="RECEIVED_TO[]" class="form-control" placeholder="To Date"
                         value="<%=receiveds_to[i]%>" required>

                  <%
                              }
                        }
                  %>
                        <%--Received_from and Received_to End--%>
                  <input type="hidden" name="pageNumber" value="${currentPage - 1}">


                  <button type="submit" class="pagination-button previous-button">Previous</button>
            </form>
      </c:if>

      <c:if test="${hasNextPage == true}">
            <form action="${pageContext.request.contextPath}/pagination" method="post">
                  <input type="hidden" name="action" value="pagination">


                        <%--Sender Start--%>
                  <%
                        if (senders != null) {
                              for (String sender : senders) {
                  %>
                  <input type="hidden" name="SENDER[]" class="form-control" placeholder="Enter Sender Email"
                         value="<%= sender %>" required>
                  <%
                              }
                        }
                  %>
                        <%--Sender Start--%>

                        <%--Recipient Start--%>
                  <%
                        if (recipients != null) {
                              for (String recipient : recipients) {
                  %>
                  <input type="hidden" name="RECIPIENT[]" class="form-control" placeholder="Enter Recipient Email"
                         value="<%= recipient %>" required>
                  <%
                              }
                        }
                  %>
                        <%--Recipient End--%>

                        <%--MessageTraceId Start--%>
                  <%
                        if (messageTraceIds != null) {
                              for (String messageTraceId : messageTraceIds) {
                  %>
                  <input type="hidden" name="MESSAGE_TRACE_ID[]" class="form-control" placeholder="Enter Message Trace ID"
                         value="<%= messageTraceId %>" required>

                  <%
                              }
                        }
                  %>
                        <%--MessageTraceId End--%>

                        <%--Subject start--%>
                  <%
                        if (subjects != null) {
                              for (String subject : subjects) {
                  %>

                  <input type="hidden" name="SUBJECT[]" class="form-control" placeholder="Enter Subject"
                         value="<%= subject %>"
                         required>

                  <%
                              }
                        }
                  %>
                        <%--Subject End--%>

                        <%--From Ip Start--%>
                  <%
                        if (fromIps != null) {
                              for (String fromIp : fromIps) {
                  %>
                  <input type="hidden" name="FROM_IP[]" class="form-control" placeholder="Enter From IP"
                         value="<%= fromIp %>"
                         required>
                  <%
                              }
                        }
                  %>
                        <%--From IP End--%>

                        <%--To IP Start--%>
                  <%
                        if (toIps != null) {
                              for (String toIp : toIps) {
                  %>
                  <input type="hidden" name="TO_IP[]" class="form-control" placeholder="Enter To IP" value="<%= toIp
             %>" required>
                  <%
                              }
                        }
                  %>
                        <%--To IP End--%>

                        <%--Sizes Start--%>
                  <%
                        if (sizes != null) {
                              for (String size : sizes) {
                  %>

                  <input type="hidden" name="SIZE[]" class="form-control" placeholder="Enter Size" value="<%= size %>"
                         required>
                  <%
                              }
                        }
                  %>
                        <%--Sizes End--%>

                        <%--Sizes_from and Sizes_to Start--%>
                  <%
                        if (sizes_from != null && sizes_to != null && sizes_from.length == sizes_to.length) {
                              for (int i = 0; i < sizes_from.length; i++) {
                  %>

                  <input type="hidden" name="SIZE_FROM[]" class="form-control" placeholder="Min Size"
                         value="<%= sizes_from[i] %>" required>
                  <input type="hidden" name="SIZE_TO[]" class="form-control" placeholder="Max Size"
                         value="<%= sizes_to[i] %>"
                         required>

                  <%
                              }
                        }
                  %>
                        <%--Sizes_from and Sizes_to End--%>

                        <%--Receiveds Start--%>
                  <%
                        if (receiveds != null) {
                              for (String received : receiveds) {
                  %>

                  <input type="hidden" name="RECEIVED[]" class="form-control" placeholder="Enter Date"
                         value="<%=received%>" required>

                  <%
                              }
                        }
                  %>
                        <%--Received End--%>

                        <%--Received_from and Received_to Start--%>
                  <%
                        if (receiveds_from != null && receiveds_to != null && receiveds_from.length == receiveds_to.length) {
                              for (int i = 0; i < receiveds_from.length; i++) {
                  %>

                  <input type="hidden" name="RECEIVED_FROM[]" class="form-control" placeholder="From Date"
                         value="<%=receiveds_from[i]%>" required>
                  <input type="hidden" name="RECEIVED_TO[]" class="form-control" placeholder="To Date"
                         value="<%=receiveds_to[i]%>" required>

                  <%
                              }
                        }
                  %>
                        <%--Received_from and Received_to End--%>



                  <input type="hidden" name="pageNumber" value="${currentPage + 1}">
                  <button type="submit" class="pagination-button next-button">Next</button>
            </form>
      </c:if>
</div>
<%-- End : Next page and previous page feature :) ends --%>

<%-- Start : Table Starts--%>
<div class="container-fluid mt-4">
      <h1 class="text-center mb-4">Message Trace Results</h1>
      <div class="table-container">
            <table class="table table-striped table-bordered">
                  <thead>
                  <tr>
                        <th scope="col">SENDER</th>
                        <th scope="col">RECIPIENT</th>
                        <th scope="col">MESSAGE_TRACE_ID</th>
                        <th scope="col">SUBJECT</th>
                        <th scope="col">FROM_IP</th>
                        <th scope="col">TO_IP</th>
                        <th scope="col">SIZE</th>
                        <th scope="col">RECEIVED</th>
                  </tr>
                  </thead>
                  <tbody>
                  <c:forEach var="mailTrace" items="${mailTraces}">
                        <tr>
                              <td>${mailTrace.SENDER}</td>
                              <td>${mailTrace.RECIPIENT}</td>
                              <td>${mailTrace.MESSAGE_TRACE_ID}</td>
                              <td>${mailTrace.SUBJECT}</td>
                              <td>${mailTrace.FROM_IP}</td>
                              <td>${mailTrace.TO_IP}</td>
                              <td>${mailTrace.SIZE}</td>
                              <td>${mailTrace.RECEIVED}</td>
                        </tr>
                  </c:forEach>
                  </tbody>
            </table>
      </div>
</div>
<%-- End : Table Ends--%>


<%--    bootstrap Js scripts start --%>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
<%--    bootstrap Js scripts end --%>

<%--    custom Js scripts start --%>
<script src="${pageContext.request.contextPath}/js/displayPageSubmissionHandler.js"></script>
<script src="${pageContext.request.contextPath}/js/filtersCreationScript.js"></script>
<%--    custom Js scripts end --%>


</body>
</html>
