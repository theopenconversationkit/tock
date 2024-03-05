import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FeatureComponent } from './feature/feature.component';
import { I18nComponent } from './i18n/i18n.component';
import { EditStoryComponent } from './story/edit-story/edit-story.component';
import { SearchStoryComponent } from './story/search-story/search-story.component';
import { CreateStoryComponent } from './story/create-story/create-story.component';
import { ApplicationResolver } from '../core-nlp/application.resolver';
import { BotTabsComponent } from './bot-tabs.component';
import { AuthGuard } from '../core-nlp/auth/auth.guard';
import { DocumentsStoryComponent } from './story/documents-story/documents-story.component';

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    component: BotTabsComponent,
    resolve: {
      application: ApplicationResolver
    },
    children: [
      {
        path: '',
        redirectTo: 'story-create',
        pathMatch: 'full'
      },
      {
        path: 'story-create',
        component: CreateStoryComponent
      },
      {
        path: 'story-search',
        component: SearchStoryComponent
      },
      {
        path: 'story-edit/:storyId',
        component: EditStoryComponent
      },
      {
        path: 'i18n',
        component: I18nComponent
      },
      {
        path: 'story-rules',
        component: FeatureComponent
      },
      {
        path: 'story-documents',
        component: DocumentsStoryComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BotRoutingModule {}
