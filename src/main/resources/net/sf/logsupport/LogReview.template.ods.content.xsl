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

<xsl:stylesheet version="1.0"
				xmlns:xhtml="http://www.w3.org/1999/xhtml"
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

	<xsl:template match="xhtml:table[@id = 'reviewTable']">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="xhtml:table"/>

	<xsl:template match="xhtml:tr">
		<xsl:if test="@id != '' and @id != 'headerRow'">
			<table:table-row table:style-name="ro2">
				<table:table-cell office:value-type="string" table:style-name="ce6">
					<text:p>
						<xsl:value-of select="@id"/>
					</text:p>
				</table:table-cell>
				<table:table-cell office:value-type="string" table:style-name="ce7">
					<text:p>
						<xsl:value-of select="xhtml:td[@class='logLevel']/text()"/>
					</text:p>
				</table:table-cell>
				<table:table-cell office:value-type="string" table:style-name="ce8">
					<text:p>
						<xsl:value-of select="xhtml:td[@class='logId']/text()"/>
					</text:p>
				</table:table-cell>
				<table:table-cell office:value-type="string" table:style-name="ce14">
					<text:p>
						<xsl:apply-templates select="xhtml:td[@class='logMessage']"/>
					</text:p>
				</table:table-cell>
				<table:table-cell office:value-type="string" table:style-name="ce9">
					<text:p>
						<xsl:apply-templates select="xhtml:td[@class='logSource']"/>
					</text:p>
				</table:table-cell>
				<table:table-cell table:number-columns-repeated="16379"/>
			</table:table-row>
		</xsl:if>
	</xsl:template>

	<xsl:template match="xhtml:td">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="xhtml:br">
		<text:line-break/>
	</xsl:template>

	<xsl:template match="xhtml:span[@class = 'constant']">
		<text:span text:style-name="T2">
			<xsl:value-of select="text()"/>
		</text:span>
	</xsl:template>

	<xsl:template match="text()">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="/">
		<office:document-content>
			<office:font-face-decls>
				<style:font-face style:name="Arial" svg:font-family="Arial"/>
			</office:font-face-decls>
			<office:automatic-styles>
				<style:style style:name="ce1" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0"/>
				<style:style style:name="ce2" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #000000"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin solid #000000" fo:border-right="none"
												 style:vertical-align="middle" fo:background-color="#254061"
												 style:cell-protect="protected"
												 style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties fo:color="#FFFFFF" style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial" fo:font-weight="bold"
										   style:font-weight-asian="bold"
										   style:font-weight-complex="bold"/>
				</style:style>
				<style:style style:name="ce3" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #000000"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="2pt solid #000000"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="middle" fo:background-color="#254061"
												 style:cell-protect="protected" style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties fo:color="#FFFFFF" style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial" fo:font-weight="bold"
										   style:font-weight-asian="bold"
										   style:font-weight-complex="bold"/>
				</style:style>
				<style:style style:name="ce4" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #000000"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="middle" fo:background-color="#254061"
												 style:cell-protect="protected" style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties fo:color="#FFFFFF" style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial" fo:font-weight="bold"
										   style:font-weight-asian="bold"
										   style:font-weight-complex="bold"/>
				</style:style>
				<style:style style:name="ce5" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #000000"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="2pt solid #000000"
												 style:vertical-align="middle" fo:background-color="#254061"
												 style:cell-protect="protected" style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties fo:color="#FFFFFF" style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial" fo:font-weight="bold"
										   style:font-weight-asian="bold"
										   style:font-weight-complex="bold"/>
				</style:style>
				<style:style style:name="ce6" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin solid #000000" fo:border-right="none"
												 style:vertical-align="top" style:cell-protect="protected"
												 style:shrink-to-fit="true"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="8pt" style:font-size-asian="8pt"
										   style:font-size-complex="8pt"/>
				</style:style>
				<style:style style:name="ce7" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="2pt solid #000000"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="top" style:cell-protect="protected"
												 style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce8" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="top" style:cell-protect="none"
												 style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce9" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="2pt solid #000000"
												 style:vertical-align="top" fo:wrap-option="wrap"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce10" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin solid #000000" fo:border-right="none"
												 style:vertical-align="top" style:cell-protect="protected"
												 style:shrink-to-fit="true"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce11" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="2pt solid #000000"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="top" style:cell-protect="protected"
												 style:shrink-to-fit="true" style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce12" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="top" style:cell-protect="none"
												 style:shrink-to-fit="true"
												 style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce13" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="2pt solid #000000"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce14" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N30">
					<style:table-cell-properties fo:border-top="thin solid #7F7F7F"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="top" fo:wrap-option="wrap"
												 style:cell-protect="none"
												 style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="start" fo:margin-left="0cm"/>
					<style:text-properties style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial"
										   fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"/>
				</style:style>
				<style:style style:name="ce15" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N30">
					<style:table-cell-properties fo:border-top="thin solid #000000"
												 fo:border-bottom="thin solid #7F7F7F"
												 fo:border-left="thin dotted #A5A5A5"
												 fo:border-right="thin dotted #A5A5A5"
												 style:vertical-align="middle" fo:background-color="#254061"
												 style:cell-protect="protected" style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties fo:color="#FFFFFF" style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial" fo:font-weight="bold"
										   style:font-weight-asian="bold"
										   style:font-weight-complex="bold"/>
				</style:style>
				<style:style style:name="ce16" style:family="table-cell" style:parent-style-name="Default"
							 style:data-style-name="N0">
					<style:table-cell-properties style:vertical-align="middle" style:cell-protect="protected"
												 style:repeat-content="false"/>
					<style:paragraph-properties fo:text-align="center"/>
					<style:text-properties fo:font-size="10pt" style:font-size-asian="10pt"
										   style:font-size-complex="10pt"
										   fo:font-weight="bold" style:font-weight-asian="bold"
										   style:font-weight-complex="bold"/>
				</style:style>
				<style:style style:name="T1" style:family="text" style:parent-style-name="Default">
					<style:text-properties fo:color="#000000" style:text-line-through-style="none"
										   style:font-name="Arial"
										   style:font-name-asian="Arial" style:font-name-complex="Arial"
										   fo:font-size="10pt"
										   style:font-size-asian="10pt" style:font-size-complex="10pt"
										   fo:font-style="normal"
										   style:font-style-asian="normal" style:font-style-complex="normal"
										   style:text-underline-style="none" style:text-underline-type="none"
										   fo:font-weight="normal" style:font-weight-asian="normal"
										   style:font-weight-complex="normal" style:text-outline="false"
										   fo:text-shadow="none"/>
				</style:style>
				<style:style style:name="T2" style:family="text" style:parent-style-name="Default">
					<style:text-properties fo:color="#FF0000" style:text-line-through-style="none"
										   style:font-name="Arial"
										   style:font-name-asian="Arial" style:font-name-complex="Arial"
										   fo:font-size="10pt"
										   style:font-size-asian="10pt" style:font-size-complex="10pt"
										   fo:font-style="normal"
										   style:font-style-asian="normal" style:font-style-complex="normal"
										   style:text-underline-style="none" style:text-underline-type="none"
										   fo:font-weight="bold" style:font-weight-asian="bold"
										   style:font-weight-complex="bold"
										   style:text-outline="false" fo:text-shadow="none"/>
				</style:style>
				<style:style style:name="co1" style:family="table-column">
					<style:table-column-properties fo:break-before="auto" style:column-width="1.5cm"/>
				</style:style>
				<style:style style:name="co2" style:family="table-column">
					<style:table-column-properties fo:break-before="auto" style:column-width="2cm"/>
				</style:style>
				<style:style style:name="co3" style:family="table-column">
					<style:table-column-properties fo:break-before="auto" style:column-width="3.3cm"/>
				</style:style>
				<style:style style:name="co4" style:family="table-column">
					<style:table-column-properties fo:break-before="auto" style:column-width="16cm"/>
				</style:style>
				<style:style style:name="co5" style:family="table-column">
					<style:table-column-properties fo:break-before="auto" style:column-width="10cm"/>
				</style:style>
				<style:style style:name="co6" style:family="table-column">
					<style:table-column-properties fo:break-before="auto" style:column-width="1.7cm"/>
				</style:style>
				<style:style style:name="ro1" style:family="table-row">
					<style:table-row-properties style:row-height="21pt" style:use-optimal-row-height="false"
												fo:break-before="auto"/>
				</style:style>
				<style:style style:name="ro2" style:family="table-row">
					<style:table-row-properties style:row-height="25.5pt" style:use-optimal-row-height="true"
												fo:break-before="auto"/>
				</style:style>
				<style:style style:name="ro3" style:family="table-row">
					<style:table-row-properties style:row-height="15pt" style:use-optimal-row-height="true"
												fo:break-before="auto"/>
				</style:style>
				<style:style style:name="ta1" style:family="table" style:master-page-name="mp1">
					<style:table-properties table:display="true" style:writing-mode="lr-tb"/>
				</style:style>
			</office:automatic-styles>
			<office:body>
				<office:spreadsheet>
					<table:calculation-settings table:case-sensitive="false"
												table:search-criteria-must-apply-to-whole-cell="false"/>
					<table:table table:name="LogReview" table:style-name="ta1" table:protected="true"
								 table:print-ranges="LogReview.B1:LogReview.E1048576">
						<table:table-column table:style-name="co1" table:default-cell-style-name="ce10"/>
						<table:table-column table:style-name="co2" table:default-cell-style-name="ce11"/>
						<table:table-column table:style-name="co3" table:default-cell-style-name="ce12"/>
						<table:table-column table:style-name="co4" table:default-cell-style-name="ce14"/>
						<table:table-column table:style-name="co5" table:default-cell-style-name="ce13"/>
						<table:table-column table:style-name="co6" table:number-columns-repeated="16379"
											table:default-cell-style-name="ce1"/>
						<table:table-row table:style-name="ro1">
							<table:table-cell office:value-type="string" table:style-name="ce2">
								<text:p>ID</text:p>
							</table:table-cell>
							<table:table-cell office:value-type="string" table:style-name="ce3">
								<text:p>Level</text:p>
							</table:table-cell>
							<table:table-cell office:value-type="string" table:style-name="ce4">
								<text:p>Log ID</text:p>
							</table:table-cell>
							<table:table-cell office:value-type="string" table:style-name="ce15">
								<text:p>Log Message</text:p>
							</table:table-cell>
							<table:table-cell office:value-type="string" table:style-name="ce5">
								<text:p>Source file(s)</text:p>
							</table:table-cell>
							<table:table-cell table:number-columns-repeated="16379" table:style-name="ce16"/>
						</table:table-row>

						<xsl:apply-templates select="//xhtml:table"/>

						<table:table-row table:number-rows-repeated="64" table:style-name="ro3">
							<table:table-cell table:number-columns-repeated="16384"/>
						</table:table-row>
					</table:table>
				</office:spreadsheet>
			</office:body>
		</office:document-content>
	</xsl:template>
</xsl:stylesheet>