package view;

import com.view.utils.ADFUtils;

import javax.faces.event.ActionEvent;

import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewObjectImpl;

public class SearchPayroll {
    private RichOutputText messageBind;
    private RichTable payrolltable;

    public SearchPayroll() {
    }
    private RichPopup myPopup;
    public String message = "";


    public void onClickSubmit(ActionEvent actionEvent) {

        ViewObject HdrVO = ADFUtils.findIterator("Payroll_ROVOIterator").getViewObject();
        ViewObjectImpl VO = (ViewObjectImpl) ADFUtils.findIterator("DutyAllowance_VOIterator").getViewObject();
        RowSetIterator iter = HdrVO.createRowSetIterator(null);
        System.err.println(HdrVO.getCurrentRow().getAttribute("Trans_Active"));
        while (iter.hasNext()) {
            Row r = iter.next();

            if (r.getAttribute("Trans_Active") != null && r.getAttribute("Trans_Active").equals(true)) {
                System.err.println("staus==" + r.getAttribute("Trans_Active"));
                System.err.println(r.getAttribute("ExtraDutyAllowanceId"));
                ViewCriteria vc = VO.getViewCriteria("findById");
                VO.applyViewCriteria(vc);
                VO.setNamedWhereClauseParam("BV_Id", r.getAttribute("ExtraDutyAllowanceId"));
                VO.executeQuery();
                System.err.println("count1--------" + VO.getEstimatedRowCount());
                if (VO.getEstimatedRowCount() > 0) {
                    System.err.println("inside");
                    VO.first().setAttribute("ApprovalStatus", "SUBMITTED FOR PAYROLL");
                    ADFUtils.findOperation("Commit").execute();
                    
                }
            }
        }
        
        
        String xmlData = null;
        String[] a = null;
        String payRollWSDL = null;
        payRollWSDL = ApprovalPayload.PAYROLL_WSDL;

        String p_EmployeeNumber = "Test";

        xmlData = ApprovalPayload.payrollTypeXMLData(p_EmployeeNumber);

        System.err.println("xmlData =>" + xmlData);
        a = ApprovalProcess.invokeWsdl(xmlData, payRollWSDL, ApprovalPayload.PAYROLL_METHOD);
        if (a[0] != null && a[0].equals("True")) {
            ADFUtils.findOperation("Commit").execute();
            ADFContext.getCurrent()
                                   .getPageFlowScope()
                                   .put("showMsg" ,"Extra duty Submitted to Payroll Successfully");
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            myPopup.show(hints);
            AdfFacesContext.getCurrentInstance().addPartialTarget(messageBind);
           // JSFUtils.addFacesInformationMessage("Payroll Submitted Successfully");

        } else {            
            ADFContext.getCurrent()
                                   .getPageFlowScope()
                                   .put("showMsg" ,"Error While submitting Payroll");
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            myPopup.show(hints);
            AdfFacesContext.getCurrentInstance().addPartialTarget(messageBind);
           // JSFUtils.addFacesInformationMessage("Error");

        }
    }

    public void hidePopup(ActionEvent actionEvent) {
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        getMyPopup().hide();
    }

    public void setMyPopup(RichPopup myPopup) {
        this.myPopup = myPopup;
    }

    public RichPopup getMyPopup() {
        return myPopup;
    }
   

    public void setMessageBind(RichOutputText messageBind) {
        this.messageBind = messageBind;
    }

    public RichOutputText getMessageBind() {
        return messageBind;
    }

    public void onClickSelectAll(ActionEvent actionEvent) {
        ViewObject HdrVO = ADFUtils.findIterator("Payroll_ROVOIterator").getViewObject();
        Row[] selectedrow = HdrVO.getFilteredRows("ApprovalStatus", "APPROVED");
        for (Row row : selectedrow) {
            row.setAttribute("Trans_Active","true");
        }
    }

    public void setPayrolltable(RichTable payrolltable) {
        this.payrolltable = payrolltable;
    }

    public RichTable getPayrolltable() {
        return payrolltable;
    }
}
