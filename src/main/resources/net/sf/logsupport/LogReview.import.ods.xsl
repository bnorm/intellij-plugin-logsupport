<?xml version="1.0" encoding="UTF-8" standalone="yes"?>

<!--
  ~ Copyright 2010, Juergen Kellerer and other contributors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml"
				xmlns:xlink="http://www.w3.org/1999/xlink"
				xmlns:dc="http://purl.org/dc/elements/1.1/"
				xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
				xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
				xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
				xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
				xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
				xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
				xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
				xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" version="1.0" encoding="utf-8"
				doctype-system="xhtml1-transitional.dtd"
				doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>

	<xsl:template match="table:table">
		<xsl:apply-templates select="table:table-row[position() > 1 and count(table:table-cell) = 6]"/>
	</xsl:template>

	<xsl:template match="table:table-row">
		<xsl:element name="tr">
			<xsl:attribute name="id">
				<xsl:apply-templates select="table:table-cell[1]"/>
			</xsl:attribute>
			<td class="logLevel">
				<xsl:apply-templates select="table:table-cell[2]"/>
			</td>
			<td class="logId">
				<xsl:apply-templates select="table:table-cell[3]"/>
			</td>
			<td class="logMessage">
				<xsl:apply-templates select="table:table-cell[4]"/>
			</td>
			<td class="logSource">
				<xsl:apply-templates select="table:table-cell[5]"/>
			</td>
		</xsl:element>
	</xsl:template>

	<xsl:template match="text:s">
		<xsl:value-of select="' '"/>
	</xsl:template>

	<xsl:template match="table:*|text:*|number:*">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="text()">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="/">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head/>

			<body>
				<table id="reviewTable" border="1" cellspacing="0" cellpadding="3" width="100%">
					<thead>
						<tr id="headerRow">
							<th align="left">Level</th>
							<th align="left">ID</th>
							<th align="left">Message</th>
							<th align="left">Source file(s)</th>
						</tr>
					</thead>
					<tbody id="templateRow">
						<xsl:apply-templates select="//table:table[@table:name='LogReview']"/>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>