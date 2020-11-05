/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import {Component, Input, OnInit} from '@angular/core';
import {Story} from './model/Story';
import {Interaction} from './model/Interaction';

@Component({
  selector: 'tock-story-builder',
  templateUrl: './story-builder.component.html',
  styleUrls: ['./story-builder.component.css']
})
export class StoryBuilderComponent implements OnInit {

  @Input()
  story: Story;

  currentPath: Interaction[];

  ngOnInit(): void {
    this.story = {
      name: 'Je ne trouve pas mon train',
      intent: 'selfcare_notrain_found',
      interactions: [
        new Interaction('question_1',
          'selfcare_notrain_found',
          'Je ne trouve pas mon train',
          [
            {
              type: 'bot',
              content: [
                {
                  type: 'text',
                  text: 'Votre trajet se déroule en Ile-de-France ?'
                }
              ],
              interactions: [
                new Interaction('yes_1', 'yes', 'Oui', [
                  {
                    type: 'bot',
                    content: [
                      {
                        type: 'text',
                        text: 'Quel est votre mode de transport ?',
                      },
                    ],
                    interactions: []
                  },
                  {
                    type: 'user',
                    content: [
                      {
                        'type': 'text',
                        'text': 'Je pars de Paris Saint-Lazare',
                      }
                    ],
                    interactions: []
                  }
                ]),
                new Interaction('no_1', 'no', 'Non', [
                    {
                      'type': 'user',
                      'content': [
                        {
                          'type': 'text',
                          'text': 'Non'
                        }
                      ],
                      interactions: []
                    },
                    {
                      'type': 'bot',
                      'content': [
                        {
                          'type': 'text',
                          'text': 'Désolé, nous ne supportons que les trajets IDF'
                        }
                      ],
                      interactions: []
                    }
                  ]
                )
              ]
            }
          ])
      ]
    }
    ;
    this.currentPath = [
      this.story.interactions[0]
      , this.story.interactions[0].entries[0].interactions[0]
    ];
  }

  addUserSentence = () => {
    console.log('User sentence');
  }

  addUserAction = () => {
    console.log('Add user action');
  }

  addBotResponse = () => {
    console.log('Add bot response');
  }

  save() {
    console.log('Saved the story');
  }
}
