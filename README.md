# SSM Data Persist

### This application is intended for demonstration purposes only. It is provided as-is without guarantee or warranty and may be modified to suit individual needs.
=========================================================

This sample app demonstrates how to use Secure Storage Manager (SSM) to securely share data. 

- Optionally, the data can be configured to persist through an enterprise reset
- DET pre-encrypted data sharing is also shown 

Refer to Zebra TechDocs: http://techdocs.zebra.com/datawedge/latest/guide/samples/share-data/

For DET, refer to
- ORIGINATING APP SENDING DET-ENCRYPTED DATA: https://github.com/NDZL/A13_SSM_DATA/blob/f6bc022db016db33039427c827720dfbee5a85d8/app/src/main/java/com/zebra/ssmdatapersist/SSMDataProcessor.java#L160
- TARGET APP CONSUMING DATA (AUTOMATICALLY DECRYPTED): https://github.com/ZebraDevs/A13-Companion-App/blob/aa64b537e098bd3286203a92424ce08e4cd5dec5/app/src/main/java/com/ndzl/sst_companionapp/MainActivity.java#L346
- DET user manual: https://techdocs.zebra.com/det/1-0/guide/about/ 
