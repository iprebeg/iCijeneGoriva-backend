<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/xml; charset=UTF-8" pageEncoding="UTF-8" import="com.prebeg.cijenegoriva.model.*,java.util.List"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<cjenik>
	<c:forEach var="gorivo" items="${cjenik.goriva}">
		<gorivo>
				<naziv>${gorivo.naziv}</naziv>
				<distributer>${gorivo.distributer}</distributer>
				<cijena>${gorivo.cijena}</cijena>
				<kategorija>${gorivo.kategorija}</kategorija>
				<autocesta>${gorivo.autocesta}</autocesta>
				<datum>${gorivo.datum}</datum>
		</gorivo>
	</c:forEach>
</cjenik>
