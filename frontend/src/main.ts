import { initHeronSignal } from '@heronsignal/web';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

void initHeronSignal({ publicKey: 'pk_bSB8cLlH-JALIy9ZF0nZA18okA55kmVE' });

bootstrapApplication(App, appConfig).catch((err) => console.error(err));
